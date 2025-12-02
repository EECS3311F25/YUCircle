package main.entity;

import main.entity.CourseSession;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "course",
        uniqueConstraints = @UniqueConstraint(columnNames = {"courseCode", "courseSection"}))
@Getter
@Setter
@NoArgsConstructor // Adds a no-argument constructor (required by JPA)
@AllArgsConstructor // Adds a constructor with all fields
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Course {
    String courseName;
    String courseCode;
    char courseSection;
}
