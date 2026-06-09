package pe.edu.tecsup.lms.shared.infrastructure.eventsourcing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import pe.edu.tecsup.lms.shared.domain.event.DomainEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class MemoryEventStore implements EventStore{

    // final es necesario

    private final Map<String, List<DomainEvent>> stores = new ConcurrentHashMap<>();

    private final ApplicationEventPublisher publisher;

    public MemoryEventStore(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * Graba un evento
     * @param aggregateId
     * @param event
     */
    @Override
    public void save(String aggregateId, DomainEvent event) {

        // Agregar el evento  al stores
        this.stores.computeIfAbsent(aggregateId, key -> new ArrayList<>())
                .add(event);

        // Publicar el evento
        publisher.publishEvent(event);
    }


    /**
     *
     * @param aggregateId
     * @return
     */
    @Override
    public List<DomainEvent> getEvents(String aggregateId) {
        return new ArrayList<>(stores.getOrDefault(aggregateId, List.of()));

    }
}
