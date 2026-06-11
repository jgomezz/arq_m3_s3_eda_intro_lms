package pe.edu.tecsup.lms.enrollments.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pe.edu.tecsup.lms.shared.domain.event.DomainEvent;

@Getter
@RequiredArgsConstructor
public class StudentEnrolledEvent  extends DomainEvent {

    private final String enrollmentId;
    private final String studentId;
    private final String enrollmentName;
    private final String courseId;

}
