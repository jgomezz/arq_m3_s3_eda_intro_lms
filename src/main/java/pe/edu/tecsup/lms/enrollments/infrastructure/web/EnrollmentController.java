package pe.edu.tecsup.lms.enrollments.infrastructure.web;

import org.springframework.http.HttpStatus;
import pe.edu.tecsup.lms.enrollments.application.command.EnrollStudentCommand;
import pe.edu.tecsup.lms.enrollments.application.command.EnrollmentCommandHandler;
import pe.edu.tecsup.lms.enrollments.application.query.EnrollmentQueryRepository;
import pe.edu.tecsup.lms.enrollments.application.query.EnrollmentReadModel;
import pe.edu.tecsup.lms.enrollments.application.saga.EnrollmentSagaHandler;
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

    private final EnrollmentQueryRepository enrollmentQueryRepository;



    // ========================================
    // SAGA
    // ========================================

    private final EnrollmentSagaHandler sagaHandler;

    @PostMapping("/request")
    public ResponseEntity<EnrollmentResponse> requestEnrollment(
            @RequestBody EnrollmentRequest request) {

        // Iniciar saga
        String enrollmentId = this.sagaHandler.requestEnrollment(request.getStudentId(),
                request.getStudentName(),
                request.getCourseId(),
                request.getAmount());

        EnrollmentResponse response = EnrollmentResponse.builder()
                .enrollmentId(enrollmentId)
                .status("PENDING")
                .message("Enrollment request is being processed")
                .build();

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(response);
    }


    /**
     *  Enroll a student in a course
     */
    @PostMapping
    public ResponseEntity<EnrollmentResponse> enrollStudent(@RequestBody EnrollmentRequest request) {

        EnrollStudentCommand command
                = EnrollStudentCommand.builder()
                .studentId(request.getStudentId())
                .studentName(request.getStudentName())
                .courseId(request.getCourseId())
                .build();

        String enrollmentId = enrollmentCommandHandler.enrollStudent(command);

        // return ResponseEntity.ok(new EnrollmentResponse(enrollmentId));
        return ResponseEntity.ok(EnrollmentResponse
                .builder()
                .enrollmentId(enrollmentId)
                .build());

    }

    /**
     *  Agregar una lesson al curso
     *  Cada lesson agrega un 10% de progreso al curso.
     * @param enrollmentId
     * @param lessonId
     * @return
     */
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

    // CQRS Implementation

    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentReadModel> getEnrollment(@PathVariable String id) {

        EnrollmentReadModel readModel
                = this.enrollmentQueryRepository.findByEnrollmentId(id)
                .orElseThrow(() -> new RuntimeException("No enrollment with id " + id));

        return ResponseEntity.ok(readModel);
    }



}
