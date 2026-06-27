package pe.edu.tecsup.lms.enrollments.infrastructure.dto;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EnrollmentRequest {
    private  String studentId;
    private  String studentName;
    private  String courseId;

    // Nuevo campo
    private BigDecimal amount;
}