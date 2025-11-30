package main.dto;

public record StudentDTO(
    Long studentNumber,
    String firstName,
    String lastName,
    String username,
    String email,
    String major
) {}
