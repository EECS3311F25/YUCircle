package main.service;

import main.dto.ParsedScheduleDTO;

import com.azure.ai.formrecognizer.documentanalysis.*;
import com.azure.ai.formrecognizer.documentanalysis.models.*;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.exception.HttpResponseException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.*;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class AzureOcrService {

    private final DocumentAnalysisClient client;
    private static final Logger log = LoggerFactory.getLogger(AzureOcrService.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");

    public AzureOcrService(
            @Value("${azure.formrecognizer.endpoint}") String endpoint,
            @Value("${azure.formrecognizer.key}") String key
    ) {
        this.client = new DocumentAnalysisClientBuilder()
                .credential(new AzureKeyCredential(key))
                .endpoint(endpoint)
                .buildClient();
    }

    public List<ParsedScheduleDTO> extractScheduleFromFile(MultipartFile file) {
        try {
            BinaryData data = BinaryData.fromStream(file.getInputStream(), file.getSize());
            AnalyzeResult result = client.beginAnalyzeDocument("prebuilt-layout", data).getFinalResult();
            return parseSchedule(result);
        } catch (IOException e) {
            // Log full stack trace
            log.error("Failed to read uploaded file", e);
            // Bubble up message for the controller
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Failed to read uploaded file: " + e.getMessage());
        } catch (HttpResponseException e) {
            log.error("Azure OCR call failed", e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Azure OCR call failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during OCR parsing", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unexpected error during OCR parsing: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------
    // PARSING FUNCTION
    // ------------------------------------------------------------

    private List<ParsedScheduleDTO> parseSchedule(AnalyzeResult result) {

        List<ParsedScheduleDTO> parsed = new ArrayList<>();

        // Extract times from column 0 for all tables
        Map<Integer, TimeRange> timeMap = extractTimeRanges(result);

        // Process each table
        for (DocumentTable table : result.getTables()) {

            // Group cells by column
            Map<Integer, List<DocumentTableCell>> columns = groupByColumn(table.getCells());

            // Process each column except column 0
            for (Map.Entry<Integer, List<DocumentTableCell>> entry : columns.entrySet()) {
                int col = entry.getKey();
                List<DocumentTableCell> cells = entry.getValue();

                if (col == 0) {
                    continue; // Skip column 0 (which has the times)
                }

                String day = inferDayFromColumn(col);
                if (day == null) {
                    continue;
                }

                // Sort cells by row index so parsing is top-down
                cells.sort(Comparator.comparingInt(DocumentTableCell::getRowIndex));

                CourseBlock block = null;

                // Process each cell in the column
                for (DocumentTableCell cell : cells) {
                    String text = clean(cell.getContent());
                    int row = cell.getRowIndex();

                    if (text.isBlank()) {
                        continue;
                    }

                    // Detect start of a block (i.e. group of course info) by course code
                    Matcher courseMatch = COURSE_PATTERN.matcher(text);
                    if (courseMatch.find()) {
                        // Finalize previous block if all its attributes are collected
                        if (block != null) {
                            finalizeBlock(block, timeMap, parsed);
                        }

                        // Start new block
                        block = new CourseBlock();
                        block.courseCode = normalizeCourseCode(courseMatch.group());
                        block.day = day;
                        block.row = row;

                        // Capture startTime
                        TimeRange t = timeMap.get(row);
                        if (t != null) {
                            block.startTime = t.start();
                        }
                    }

                    // Find attributes for a block
                    if (block != null) {
                        Matcher sec = SECTION_PATTERN.matcher(text);
                        if (sec.find()) {
                            block.section = sec.group(1);
                        }

                        Matcher type = TYPE_PATTERN.matcher(text);
                        if (type.find()) {
                            block.type = type.group(1);

                            // Capture endTime only after type is detected
                            TimeRange t = timeMap.get(row);
                            if (t != null) {
                                block.endTime = t.end();
                            }
                        }

                        Matcher loc = LOCATION_PATTERN.matcher(text);
                        if (loc.find()) {
                            block.location = loc.group(1);
                        }
                    }
                }

                // Finalize last block in column
                if (block != null) {
                    finalizeBlock(block, timeMap, parsed);
                }
            }
        }

        // Show parsed info in log
        log.info("Parsed schedule: {}", parsed);
        return parsed;
    }

    // ------------------------------------------------------------
    // HELPERS
    // ------------------------------------------------------------

    /**
     * Groups cells by column
     */
    private Map<Integer, List<DocumentTableCell>> groupByColumn(List<DocumentTableCell> cells) {
        Map<Integer, List<DocumentTableCell>> map = new HashMap<>();
        for (DocumentTableCell c : cells) {
            map.computeIfAbsent(c.getColumnIndex(), k -> new ArrayList<>()).add(c);
        }
        return map;
    }

    /**
     * Cleans cell text
     */
    private String clean(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("[\\r\\n]+", " ")
                .replaceAll("\\s{2,}", " ")
                .replace("_", " ")
                .trim();
    }

    private static String normalizeCourseCode(String raw) {
        if (raw == null) return null;
        return raw.trim()
                .toUpperCase()
                .replaceAll("\\s+", "")   // remove spaces
                .replaceAll("-", "");     // remove dashes
    }

    /**
     * Extracts times from column 0 for ALL tables.
     */
    private Map<Integer, TimeRange> extractTimeRanges(AnalyzeResult result) {

        Map<Integer, TimeRange> map = new HashMap<>();
        Pattern timePattern = Pattern.compile("(\\d{1,2}:\\d{2})\\s*-\\s*(\\d{1,2}:\\d{2})");

        for (DocumentTable table : result.getTables()) {
            for (DocumentTableCell cell : table.getCells()) {
                if (cell.getColumnIndex() != 0) {
                    continue;
                }

                String text = clean(cell.getContent());
                Matcher m = timePattern.matcher(text);

                if (m.find()) {
                    LocalTime start = LocalTime.parse(m.group(1), TIME_FORMATTER);
                    LocalTime end = LocalTime.parse(m.group(2), TIME_FORMATTER);
                    map.put(cell.getRowIndex(), new TimeRange(start, end));
                }
            }
        }
        return map;
    }

    /**
     * Build DTO from course block.
     */
    private void finalizeBlock(CourseBlock b,
                               Map<Integer, TimeRange> timeMap,
                               List<ParsedScheduleDTO> out) {

        if (!b.isComplete()) {
            return;
        }

        TimeRange t = timeMap.get(b.row);
        if (t == null) {
            return;
        }

        out.add(new ParsedScheduleDTO(
                b.courseCode,
                b.section,
                b.type,
                b.day,
                b.startTime,
                b.endTime,
                b.location
        ));
    }

    /**
     * Simple day inference based on column index.
     * Adjust based on your table layout.
     */
    private String inferDayFromColumn(int col) {
        return switch (col) {
            case 1 -> "Monday";
            case 2 -> "Tuesday";
            case 3 -> "Wednesday";
            case 4 -> "Thursday";
            case 5 -> "Friday";
            default -> null;
        };
    }

    // ------------------------------------------------------------
    // DATA STRUCTURES & REGEX
    // ------------------------------------------------------------

    // E.g. EECS 3311 or EECS3311
    private static final Pattern COURSE_PATTERN =
            Pattern.compile("\\b[A-Z]{3,}\\s?\\d{4}\\b");

    // E.g. Section A
    private static final Pattern SECTION_PATTERN =
            Pattern.compile("\\bSection\\s([A-Z]{1,2})\\b");

    // E.g. Term F Lecture
    private static final Pattern TYPE_PATTERN =
            Pattern.compile("\\bTerm\\s[A-Z]\\s([A-Za-z]+(?:\\s[A-Za-z]+)*)\\b");

    // E.g. [LSB 106]
    private static final Pattern LOCATION_PATTERN =
            Pattern.compile("\\[(.*?)\\]|\\bFully Online\\b");

    // Already has constructor, equals(), hashCode(), toString(), and accessor methods because it's a record
    private record TimeRange(LocalTime start, LocalTime end) {}

    private static class CourseBlock {
        String courseCode;
        String section;
        String type;
        String location;
        String day;
        LocalTime startTime;
        LocalTime endTime;
        int row; // Row where course code was detected

        boolean isComplete() {
            return courseCode != null &&
                    section != null &&
                    type != null &&
                    location != null &&
                    day != null &&
                    startTime != null &&
                    endTime != null;
        }
    }
}