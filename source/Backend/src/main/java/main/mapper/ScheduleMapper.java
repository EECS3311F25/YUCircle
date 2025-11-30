package main.mapper;

import main.dto.ParsedScheduleDTO;
import main.entity.Course;
import main.entity.CourseSession;

import java.time.LocalTime;

public class ScheduleMapper {
    public static Course toCourse(ParsedScheduleDTO dto) {
        return new Course(dto.courseCode(), dto.section());
    }

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
}