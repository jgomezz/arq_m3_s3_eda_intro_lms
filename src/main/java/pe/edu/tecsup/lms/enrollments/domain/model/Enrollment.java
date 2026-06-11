package pe.edu.tecsup.lms.enrollments.domain.model;

import lombok.Getter;
import pe.edu.tecsup.lms.enrollments.domain.event.LessonCompletedEvent;
import pe.edu.tecsup.lms.enrollments.domain.event.StudentEnrolledEvent;
import pe.edu.tecsup.lms.shared.domain.event.DomainEvent;

import java.util.List;

@Getter
public class Enrollment {

    private String id;

    private String studentId;
    private String studentName;
    private String courseId;

    private int progressPercentage;

    public static Enrollment fromEvents(List<DomainEvent> events) {

        Enrollment enrollment = new Enrollment();

        for (DomainEvent event : events) {
            enrollment.apply(event);
        }
        return enrollment;
    }

    private void apply(DomainEvent event) {

        if (event instanceof StudentEnrolledEvent enrolledEvent) {
            this.id = enrolledEvent.getEnrollmentId();
            this.studentId = enrolledEvent.getStudentId();
            this.studentName = enrolledEvent.getStudentName();
            this.courseId = enrolledEvent.getCourseId();
        } else if (event instanceof  LessonCompletedEvent lessonCompletedEvent) {
            this.progressPercentage = lessonCompletedEvent.getNewProgressPercentage();
        } else if (event instanceof  DomainEvent  domainEvent) {
            // TO DO
        }


    }

}
