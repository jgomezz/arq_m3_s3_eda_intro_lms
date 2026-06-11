package pe.edu.tecsup.lms.enrollments.domain.event;

import lombok.Builder;
import lombok.Getter;
import pe.edu.tecsup.lms.shared.domain.event.DomainEvent;

@Getter
@Builder
public class LessonCompletedEvent extends DomainEvent {

    private final String enrollmentId;
    private final String lessonId;
    private final int newProgressPercentage;

}
