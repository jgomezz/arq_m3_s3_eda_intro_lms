package pe.edu.tecsup.lms.enrollments.application.command;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EnrollStudentCommand {

    private final String studentId;
    private final String studentName;
    private final String courseId;
}
