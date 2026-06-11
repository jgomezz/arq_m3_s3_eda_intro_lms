package pe.edu.tecsup.lms.enrollments.infrastructure.web;

import pe.edu.tecsup.lms.enrollments.application.command.EnrollStudentCommand;
import pe.edu.tecsup.lms.enrollments.application.command.EnrollmentCommandHandler;
import pe.edu.tecsup.lms.enrollments.domain.model.Enrollment;
import pe.edu.tecsup.lms.enrollments.infrastructure.dto.EnrollmentRequest;
import pe.edu.tecsup.lms.enrollments.infrastructure.dto.EnrollmentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentCommandHandler enrollmentCommandHandler;


    /**
     *  Enroll a student in a course
     */
    @PostMapping
    public ResponseEntity<EnrollmentResponse>
    enrollStudent(@RequestBody EnrollmentRequest request) {

        EnrollStudentCommand command
                = EnrollStudentCommand.builder()
                .studentId(request.getStudentId())
                .studentName(request.getStudentName())
                .courseId(request.getCourseId())
                .build();

        String enrollmentId = enrollmentCommandHandler.enrollStudent(command);

        return ResponseEntity.ok(new EnrollmentResponse(enrollmentId));
    }

    @PostMapping("/{enrollmentId}/lessons/{lessonId}")
    public ResponseEntity<Void> addLesson(@PathVariable String enrollmentId,
                                          @PathVariable String lessonId) {

        enrollmentCommandHandler.addLesson(enrollmentId, lessonId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{enrollmentId}/progress")
    public ResponseEntity<Void> getEnrollmentProgress(@PathVariable String enrollmentId) {
        // Lógica para obtener el progreso de la inscripción

        Enrollment enrollment = enrollmentCommandHandler.getEnrollment(enrollmentId);

        log.info("Enrollment {} - Current progress: {}%",
                enrollmentId, enrollment.getProgressPercentage());

        return ResponseEntity.ok().build();
    }

}
