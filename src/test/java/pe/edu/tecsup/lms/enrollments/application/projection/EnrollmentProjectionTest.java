package pe.edu.tecsup.lms.enrollments.application.projection;

import org.hibernate.dialect.sequence.LegacyDB2SequenceSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.edu.tecsup.lms.enrollments.application.query.EnrollmentQueryRepository;
import pe.edu.tecsup.lms.enrollments.domain.event.LessonCompletedEvent;
import pe.edu.tecsup.lms.enrollments.domain.event.StudentEnrolledEvent;

import static org.junit.jupiter.api.Assertions.*;

class EnrollmentProjectionTest {

    private EnrollmentProjection projection;
    private EnrollmentQueryRepository repository;

    @BeforeEach
    void init(){
        this.repository = new EnrollmentQueryRepository();
        this.projection = new EnrollmentProjection(this.repository);
    }

    @Test
    void onStudentEnrolled() {

        StudentEnrolledEvent event
                = StudentEnrolledEvent.builder()
                .enrollmentId("enroll-123")
                .studentId("student-123")
                .studentName("student-name")
                .courseId("course-123")
                .build();

        this.projection.onStudentEnrolled(event);

        var readModelOpt = this.repository.findByEnrollmentId("enroll-123");

        assertTrue(readModelOpt.isPresent());

        var readModel = readModelOpt.get();

        assertEquals("enroll-123", readModel.getEnrollmentId());
        assertEquals("student-123", readModel.getStudentId());
        assertEquals("student-name", readModel.getStudentName());

    }

    @Test
    void onLessonCompleted() {

        StudentEnrolledEvent event
                = StudentEnrolledEvent.builder()
                .enrollmentId("enroll-123")
                .studentId("student-123")
                .studentName("student-name")
                .courseId("course-123")
                .build();

        this.projection.onStudentEnrolled(event);

        // Simula una lesson finalizada
        LessonCompletedEvent  lessonEvent1 =  LessonCompletedEvent.builder()
                .enrollmentId("enroll-123")
                .lessonId("lesson-123")
                .newProgressPercentage(15)
                .build();

        this.projection.onLessonCompleted(lessonEvent1);

        // Simula una lesson finalizada
        LessonCompletedEvent  lessonEvent2 =  LessonCompletedEvent.builder()
                .enrollmentId("enroll-123"  )
                .lessonId("lesson-123")
                .newProgressPercentage(40)
                .build();

        this.projection.onLessonCompleted(lessonEvent2);

        var readModelOpt = this.repository.findByEnrollmentId("enroll-123");

        assertTrue(readModelOpt.isPresent());

        var readModel = readModelOpt.get();

        assertEquals(55, readModel.getProgress());

    }
}