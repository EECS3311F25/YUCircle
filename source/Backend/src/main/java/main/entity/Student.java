package main.entity;

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
@Table(name = "student")
@Getter
@Setter
@NoArgsConstructor // Adds a no-argument constructor (required by JPA)
@AllArgsConstructor // Adds a constructor with all fields
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Student {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studentNumber;

    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;

    private boolean isVerified;

    private String verificationToken;
    private String major;
    private String bio;

    @ManyToMany
    @JoinTable(
            name = "student_course",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> courses = new HashSet<>();
}