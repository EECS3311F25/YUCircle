package main.controller;

import main.dto.ParsedScheduleDTO;
import main.entity.Student;
import main.repository.StudentRepo;
import main.repository.CourseRepo;
import main.repository.CourseSessionRepo;
import main.service.AzureOcrService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "upload.max-size=4194304",
        "upload.allowed-types=application/pdf,image/jpeg,image/jpg,image/png,image/bmp,image/tiff,image/heif"
})
class StudentControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    // 👇 Mock all collaborators
    @MockBean
    private AzureOcrService ocrService;

    @MockBean
    private StudentRepo studentRepo;

    @MockBean
    private CourseRepo courseRepo;

    @MockBean
    private CourseSessionRepo sessionRepo;

    @Test
    void uploadSchedule_validPng_returns200() throws Exception {
        InputStream is = Objects.requireNonNull(
                getClass().getResourceAsStream("/schedule.png"),
                "Test resource schedule.png not found"
        );

        MockMultipartFile file = new MockMultipartFile("file", "schedule.png", "image/png", is);

        // Stub OCR service to return dummy schedule
        ParsedScheduleDTO dummy = new ParsedScheduleDTO(
                "EECS 1000", "A", "Lecture", "Monday",
                LocalTime.of(9, 0), LocalTime.of(10, 30),
                "LAS 106"
        );
        Mockito.when(ocrService.extractScheduleFromFile(Mockito.any())).thenReturn(List.of(dummy));

        // Stub StudentRepo to return a fake student
        Student fakeStudent = new Student();
        fakeStudent.setStudentNumber(1L); // primary key
        fakeStudent.setFirstName("Test");
        fakeStudent.setLastName("User");
        fakeStudent.setUsername("testuser");
        fakeStudent.setEmail("test@example.com");
        fakeStudent.setMajor("CS");

        Mockito.when(studentRepo.findById(1L)).thenReturn(Optional.of(fakeStudent));
        Mockito.when(studentRepo.save(Mockito.any(Student.class))).thenReturn(fakeStudent);

        // Stub CourseRepo and CourseSessionRepo to avoid DB calls
        Mockito.when(courseRepo.findByCourseCodeAndCourseSection(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Optional.empty());
        Mockito.when(courseRepo.save(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));

        Mockito.when(sessionRepo.existsByCourseAndDayAndStartTime(Mockito.any(), Mockito.anyString(), Mockito.any(LocalTime.class)))
                .thenReturn(false);
        Mockito.when(sessionRepo.save(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(multipart("/api/students/1/schedule").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("Schedule processed and saved."));
    }

    @Test
    void uploadSchedule_empty_returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "", "image/png", new byte[0]);

        mockMvc.perform(multipart("/api/students/1/schedule").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadSchedule_unsupportedType_returns415() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "bad.txt", "text/plain", "hello".getBytes());

        mockMvc.perform(multipart("/api/students/1/schedule").file(file))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void uploadSchedule_tooLarge_returns413() throws Exception {
        byte[] bigFile = new byte[5 * 1024 * 1024]; // 5 MB dummy data

        MockMultipartFile file = new MockMultipartFile("file", "big.png", "image/png", bigFile);

        mockMvc.perform(multipart("/api/students/1/schedule").file(file))
                .andExpect(status().isPayloadTooLarge());
    }
}
