package main.repository;

import main.dto.AvailabilityDTO;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import main.entity.Course;
import main.entity.CourseSession;
import java.time.LocalTime;
import java.util.List;

public interface CourseSessionRepo extends JpaRepository<CourseSession, Long> {
    // Check if a course session exists
    boolean existsByCourseAndDayAndStartTime(Course course, String day, LocalTime startTime);

    // Get the associated course by the student username
    List<CourseSession> findByCourse_Students_Username(String username);

    // Get blocks where the students are busy
    @Query("""
        SELECT new main.dto.AvailabilityDTO(
            cs.day,
            cs.startTime,
            cs.endTime,
            COUNT(DISTINCT s.studentNumber)
        )
        FROM CourseSession cs
        JOIN cs.course c
        JOIN c.students s
        GROUP BY cs.day, cs.startTime, cs.endTime
    """)
        List<AvailabilityDTO> findBusyBlocks();
}