package pe.edu.tecsup.lms.enrollments.application.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.edu.tecsup.lms.enrollments.domain.event.LessonCompletedEvent;
import pe.edu.tecsup.lms.enrollments.domain.event.StudentEnrolledEvent;
import pe.edu.tecsup.lms.enrollments.domain.model.Enrollment;
import pe.edu.tecsup.lms.shared.infrastructure.eventsourcing.MemoryEventStore;

@Slf4j
@RequiredArgsConstructor
public class EnrollmentCommandHandler {

    private final MemoryEventStore eventStore;

    /**
     * Enrollment to student
     * @param command datos enviado por el controlador
     * @return
     */
    public String enrollStudent(EnrollStudentCommand command) {

        String enrollmentId = "enrollment-" + System.currentTimeMillis();

        // Crear el evento de inscripción
        StudentEnrolledEvent event
                =  StudentEnrolledEvent.builder()
                .enrollmentId(enrollmentId)
                .studentId(command.getStudentId())
                .studentName(command.getStudentName())
                .courseId(command.getCourseId())
                .build();

        //
        this.eventStore.save(enrollmentId, event);

        return enrollmentId;

    }

    /**
     *
     *
     */
      public void addLesson(String enrollmentId, String lessonId) {

          // 1. Obtener todos los eventos de un enrollment id
          var events = this.eventStore.getEvents(enrollmentId);

          // 2. Reconstruir el estado actual del Enrollemnt : Event Sourcing
          var enrollment = Enrollment.fromEvents(events);

          // 3. Calcular el nuevo progreso . Logica del negocio
           int newProgress =  enrollment.getProgressPercentage() + 10;

           log.info("Adding lesson {} to enrollment {} with progress {} ", lessonId, enrollmentId, newProgress);

          // 4. Crear el nuevo evento para registrar la lesson.
           var eventLesson   = LessonCompletedEvent.builder()
                   .enrollmentId(enrollmentId)
                   .lessonId(lessonId)
                   .newProgressPercentage(newProgress)
                   .build();

           // 5. Almacenar el evento en el Event Store
          this.eventStore.save(enrollmentId, eventLesson);

      }


      public Enrollment getEnrollment(String enrollmentId) {

          // 1. Obtener todos los eventos de un enrollment id
          var events = this.eventStore.getEvents(enrollmentId);

          // 2. Reconstruir el estado actual del Enrollemnt
          var enrollment = Enrollment.fromEvents(events);

          return enrollment;
      }

}
