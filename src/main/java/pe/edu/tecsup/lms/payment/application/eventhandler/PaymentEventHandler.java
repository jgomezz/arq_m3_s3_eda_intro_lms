package pe.edu.tecsup.lms.payment.application.eventhandler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import pe.edu.tecsup.lms.courses.domain.event.CoursePublishedEvent;
import pe.edu.tecsup.lms.shared.infrastructure.config.RabbitMQConfig;
import pe.edu.tecsup.lms.shared.infrastructure.dlq.DeadLetterQueue;

import java.util.Random;

@Slf4j
@RequiredArgsConstructor  // Agregar constructor para inyección de dependencias
@Component
public class PaymentEventHandler {

    private final Random random = new Random();
    private final DeadLetterQueue dlq;  // Inyectar la DLQ

    @RabbitListener ( queues = RabbitMQConfig.PAYMENT_QUEUE)
    public void handleCoursePublished(CoursePublishedEvent event) throws InterruptedException {

        log.info("Processing payment ........ : {}", event);

        log.info("[{}] Processing payment ...", Thread.currentThread().getName());

        if (this.random.nextBoolean()) {
            log.error("Processing payment take longer times ........ : {}", event);
            throw new RuntimeException("Payment failed due to timeout");
        } else {
            log.info("Payment successfully processed");
        }

    }

    /*
    @Recover
    public void recover(RuntimeException e,  CoursePublishedEvent event ) {
        //
        log.error("All retries out for recover exception : {}", e.getMessage());

        // Add event failed to DLQ
        dlq.add(event, e);
    }
    */
}
