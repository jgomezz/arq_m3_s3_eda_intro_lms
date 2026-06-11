package pe.edu.tecsup.lms.enrollments.infrastructure.dto;
import lombok.Data;

@Data
public class EnrollmentRequest {
    private  String studentId;
    private  String studentName;
    private  String courseId;
}