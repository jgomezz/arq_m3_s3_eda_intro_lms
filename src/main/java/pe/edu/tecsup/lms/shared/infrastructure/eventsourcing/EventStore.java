package pe.edu.tecsup.lms.shared.infrastructure.eventsourcing;

import pe.edu.tecsup.lms.shared.domain.event.DomainEvent;
import java.util.List;

public interface EventStore {

    void save(String aggregateId, DomainEvent event);

    List<DomainEvent> getEvents(String aggregateId);

}
