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