package pe.edu.tecsup.lms.enrollments.application.projection;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pe.edu.tecsup.lms.enrollments.application.query.EnrollmentQueryRepository;
import pe.edu.tecsup.lms.enrollments.application.query.EnrollmentReadModel;
import pe.edu.tecsup.lms.enrollments.domain.event.LessonCompletedEvent;
import pe.edu.tecsup.lms.enrollments.domain.event.StudentEnrolledEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnrollmentProjection {

    private final EnrollmentQueryRepository repository;

    /**
     *  Listening of StudentEnrolledEvent
     */
    @EventListener
    public void  onStudentEnrolled(StudentEnrolledEvent event) {

        log.info("EnrollmentProjection.onStudentEnrolled(event={})", event);


        var model = EnrollmentReadModel.builder()
                .enrollmentId(event.getEnrollmentId())
                .courseId(event.getCourseId())
                .studentId(event.getStudentId())
                .studentName(event.getStudentName())
                .progress(0)
                .build();

        this.repository.save(model);
    }

    /**
     *  Listening of LessonCompletedEvent
     */
    @EventListener
    public void  onLessonCompleted(LessonCompletedEvent event) {

        log.info("EnrollmentProjection.onLessonCompleted(event={})", event);

        // Buscar la INFO
        EnrollmentReadModel readModel
                = this.repository.findByEnrollmentId(event.getEnrollmentId()).orElseThrow();

        // Actualiza le progresso de la lección
        var newProgress = readModel.getProgress() + event.getNewProgressPercentage();

        readModel.setProgress(newProgress);

        // Guardar el objeto actualizado
        this.repository.save(readModel);

    }







}
