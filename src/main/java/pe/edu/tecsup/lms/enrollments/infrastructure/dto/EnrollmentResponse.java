package pe.edu.tecsup.lms.enrollments.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class EnrollmentResponse {

    private String enrollmentId;

    // nuevos campos
    private String status;
    private String message;
}
