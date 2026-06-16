package pe.edu.tecsup.lms.enrollments.application.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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
    private int progress;


}
