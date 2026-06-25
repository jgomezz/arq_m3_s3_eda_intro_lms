package pe.edu.tecsup.lms.payment.application.eventhandler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pe.edu.tecsup.lms.courses.domain.event.CoursePublishedEvent;
import pe.edu.tecsup.lms.shared.domain.event.DomainEvent;
import pe.edu.tecsup.lms.shared.infrastructure.config.KafkaConfig;
import pe.edu.tecsup.lms.shared.infrastructure.dlq.DeadLetterQueue;

import java.util.Random;

@Slf4j
@RequiredArgsConstructor  // Agregar constructor para inyección de dependencias
@Component
public class PaymentEventHandler {

    private final Random random = new Random();
    private final DeadLetterQueue dlq;  // Inyectar la DLQ

    @RetryableTopic(
            attempts = "2", // Numero de reintentos
            backoff = @Backoff(
                    delay = 2000,      // Tiempo inicial de espera
                    multiplier = 2.0   // Multiplicador exponencial
            ),
            autoCreateTopics = "false",
            dltTopicSuffix = "-dlt", // Sufijo para el topico de DLQ
            include = RuntimeException.class
    )
    @KafkaListener(
            topics = KafkaConfig.COURSE_EVENTS_TOPIC, // Topico a escuchar
            groupId = "payment-service-group"    // Grupo de consumidores
    )
    public void handleCourseEvents(DomainEvent event) throws InterruptedException  {
        if (event instanceof CoursePublishedEvent) {
            handleCoursePublished((CoursePublishedEvent) event);
        }

    }


    /**
     *
     * @param event
     * @throws InterruptedException
     */
    public void handleCoursePublished(CoursePublishedEvent event) throws InterruptedException {

        log.info("Processing payment ........ : {}", event);

        if (this.random.nextBoolean()) {
            log.error("Processing payment take longer times ........ : {}", event);
            throw new RuntimeException("Payment failed");
        } else {
            log.info("Payment successfully processed");
        }

    }

    /**
     * Manejador de Dead Letter Queue
     * @param event
     * @param e
     */
    @DltHandler
    public void dltHandler(
            DomainEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.EXCEPTION_MESSAGE) String errorMessage) {

        log.error("[PAYMENT-DLT] All retries exhausted - Sending to DLQ");

        // Enviar a DLQ para procesamiento manual
        RuntimeException exception = new RuntimeException(errorMessage);
        dlq.add(event, exception, topic, offset);
    }

}
