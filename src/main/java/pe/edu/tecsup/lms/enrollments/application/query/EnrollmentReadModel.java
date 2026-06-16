package pe.edu.tecsup.lms.enrollments.application.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Builder
public class EnrollmentReadModel {

    private final String enrollmentId;
    private final String studentId;
    private final String courseId;

    // Data desnormalizada
    private final String studentName;

    // Lesson
    @Setter
    private int progress;

}
