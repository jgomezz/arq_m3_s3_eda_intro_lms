package pe.edu.tecsup.lms.shared.infrastructure.dlq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pe.edu.tecsup.lms.shared.domain.event.DomainEvent;
import pe.edu.tecsup.lms.shared.infrastructure.config.KafkaConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;


@Slf4j
@Component
@RequiredArgsConstructor
public class DeadLetterQueue {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Colección para almacenar eventos fallidos
    private final ConcurrentLinkedQueue<FailedEvent> failedEvents = new ConcurrentLinkedQueue<>();

    // Método para agregar un evento fallido a la DLQ
    public void add(DomainEvent event, Exception exception, String originalTopic, long originalOffset) {

        // Crear un objeto FailedEvent con detalles del evento fallido
        FailedEvent failedEvent = new FailedEvent(
                event,
                exception.getMessage(),
                System.currentTimeMillis()
        );

        // Agregar el evento fallido a la cola
        failedEvents.add(failedEvent);

        log.info("Event added to DLQ: {} [{}]", event.getEventType(), event.getEventId());

        // Enviar a Kafka DLQ topic
        sendToKafkaDLQ(event, exception, originalTopic, originalOffset);
    }

    /**
     *
     * @param event
     * @param exception
     * @param originalTopic
     * @param originalOffset
     */
    private void sendToKafkaDLQ(DomainEvent event, Exception exception, String originalTopic, long originalOffset) {

        // Crear mensaje DLQ con metadata completa
        Map<String, Object> dlqMessage = new HashMap<>();

        // Información del evento original
        dlqMessage.put("eventId", event.getEventId());
        dlqMessage.put("eventType", event.getEventType());
        dlqMessage.put("aggregateId", event.getKey());
        dlqMessage.put("originalEvent", event);

        this.kafkaTemplate.send(
                KafkaConfig.DLQ_COURSE_EVENTS_TOPIC,
                event.getKey(),
                dlqMessage
        );

        log.info("Event sent to Kafka DLQ: {} [{}]", event.getEventType(), event.getEventId());


    }

        // Método para obtener todos los eventos fallidos almacenados en la DLQ
    public List<FailedEvent> getFailedEvents() {
        return new ArrayList<>(failedEvents);
    }

}
