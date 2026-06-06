package pe.edu.tecsup.lms.shared.infrastructure.dlq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.edu.tecsup.lms.shared.domain.event.DomainEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;


@Slf4j
@Component
public class DeadLetterQueue {

    // Coleccion para almacenar eventos fallidos
    private final ConcurrentLinkedQueue<FailedEvent> failedEvents = new ConcurrentLinkedQueue<>();

    // Método para agregar un evento fallido a la DLQ
    public void add(DomainEvent event, Exception exception) {

        // Crear un objeto FailedEvent con detalles del evento fallido
        FailedEvent failedEvent = new FailedEvent(
                event,
                exception.getMessage(),
                System.currentTimeMillis()
        );

        // Agregar el evento fallido a la cola
        failedEvents.add(failedEvent);

    }

    // Metodo para obtener todos los eventos fallidos almacenados en la DLQ
    public List<FailedEvent> getFailedEvents() {
        return new ArrayList<>(failedEvents);
    }

}
