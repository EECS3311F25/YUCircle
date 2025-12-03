package main.mapper;

import main.dto.ParsedScheduleDTO;
import main.entity.Course;
import main.entity.CourseSession;

import java.time.LocalTime;

public class ScheduleMapper {
    // DTO to Course
    public static Course toCourse(ParsedScheduleDTO dto) {
        return new Course(dto.courseCode(), dto.section());
    }

    // DTO and Course to CourseSession
    public static CourseSession toSession(ParsedScheduleDTO dto, Course course) {
        return new CourseSession(
                course,
                dto.type(),
                dto.day(),
                dto.startTime(),
                dto.endTime(),
                dto.location()
        );
    }

    // CourseSession to DTO
    public static ParsedScheduleDTO toDto(CourseSession s) {
        return new ParsedScheduleDTO(
                s.getCSessionId(),
                s.getCourse().getCourseCode(),
                s.getCourse().getCourseSection(),
                s.getType(),
                s.getDay(),
                s.getStartTime(),
                s.getEndTime(),
                s.getLocation()
        );
    }
}