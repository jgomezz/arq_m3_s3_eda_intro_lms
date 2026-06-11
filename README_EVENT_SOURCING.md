# Implementación de Event Sourcing 

<img src="images/event_sourcing_step_1.png" width="300"   />


# 1.1. Crear Event Store (Almacén de evntos)

EventStore.java
```Java
package pe.edu.tecsup.lms.shared.infrastructure.eventsourcing;

import java.util.List;

public interface EventStore {

    void save(String aggregateId, DomainEvent event);

    List<DomainEvent> getEvents(String aggregateId);

}
```

MemoryEventStore.java

```java
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

```
# 1.2. Crear clase de pruebas para probar el MemoryEventStoreTest

```java
package pe.edu.tecsup.lms.shared.infrastructure.eventsourcing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import pe.edu.tecsup.lms.shared.domain.event.DomainEvent;

import static org.junit.jupiter.api.Assertions.*;

/**
 *  Eventos para pruebas
 */
class TestEvent extends DomainEvent {

    private final String data;

    public TestEvent( String data) {

        super();
        this.data = data;

    }

}

public class MemoryEventStoreTest {

    private MemoryEventStore eventStore;
    private ApplicationEventPublisher publisher;

    @BeforeEach
    void init(){
        this.publisher = Mockito.mock(ApplicationEventPublisher.class);
        this.eventStore = new MemoryEventStore(publisher);

    }

    @Test
    void save() {

        String aggregateId = "matricula-1";

        TestEvent event1 = new TestEvent("Datos de la matricula del estudiante 1");
        TestEvent event2 = new TestEvent("Datos de la matricula del estudiante 2");

        // Guardar los eventos
        this.eventStore.save(aggregateId, event1);
        this.eventStore.save(aggregateId, event2);

        // Recuperar todos los eventos
        var events = this.eventStore.getEvents(aggregateId);

        // Validar
        assertEquals(2 , events.size());

    }

}
```
