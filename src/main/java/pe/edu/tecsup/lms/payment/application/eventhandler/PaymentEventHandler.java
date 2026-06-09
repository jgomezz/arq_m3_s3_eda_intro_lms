package pe.edu.tecsup.lms.payment.application.eventhandler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pe.edu.tecsup.lms.courses.domain.event.CoursePublishedEvent;
import pe.edu.tecsup.lms.shared.infrastructure.dlq.DeadLetterQueue;

import java.util.Random;

@Slf4j
@RequiredArgsConstructor  // Agregar constructor para inyección de dependencias
@Component
public class PaymentEventHandler {

    private final Random random = new Random();
    private final DeadLetterQueue dlq;  // Inyectar la DLQ

    @Async("eventExecutor")
    @EventListener
    // Agregar caracteristicas de reintento
    @Retryable(
            maxAttempts = 1,  // Cantidad de reintentos
            backoff = @Backoff(delay = 1000,
            multiplier = 2))
    public void handleCoursePublished(CoursePublishedEvent event) throws InterruptedException {

        log.info("Processing payment ........ : {}", event);

        if (this.random.nextBoolean()) {
            log.error("Processing payment take longer times ........ : {}", event);
            throw new RuntimeException("Payment failed");
        } else {
            log.info("Payment successfully processed");
        }

    }

    @Recover
    public void recover(RuntimeException e,  CoursePublishedEvent event ) {
        //
        log.error("All retries out for recover exception : {}", e.getMessage());

        // Add event failed to DLQ
        dlq.add(event, e);
    }

}
