package pe.edu.tecsup.lms.shared.infrastructure.dlq;


import lombok.AllArgsConstructor;
import lombok.Getter;
import pe.edu.tecsup.lms.shared.domain.event.DomainEvent;

@Getter
@AllArgsConstructor
public class FailedEvent {

    private final DomainEvent event;
    private final String message;
    private final long timestamp;

}
