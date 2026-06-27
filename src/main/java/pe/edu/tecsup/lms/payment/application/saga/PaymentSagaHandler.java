package pe.edu.tecsup.lms.payment.application.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.tecsup.lms.enrollments.domain.event.EnrollmentRequestedEvent;
import pe.edu.tecsup.lms.payment.domain.event.PaymentFailedEvent;
import pe.edu.tecsup.lms.payment.domain.event.PaymentProcessedEvent;
import pe.edu.tecsup.lms.shared.infrastructure.config.KafkaConfig;
import pe.edu.tecsup.lms.shared.infrastructure.event.KafkaEventPublisher;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentSagaHandler {

    private final KafkaEventPublisher kafkaEventPublisher;
    private final Random random = new Random();

    @KafkaListener(
            topics = KafkaConfig.ENROLLMENT_REQUEST_TOPIC,  //"enrollment.requested",
            groupId = "payment-service-group"
    )
    @Transactional
    public void handleEnrollmentRequested(EnrollmentRequestedEvent event) {

        log.info("[PAYMENT] Procesando pago para enrollment");
        log.info("Enrollment requested: {}", event);

        // Simulamos process exitoso y fallidos

        try {

            Thread.sleep(1000 + random.nextInt(2000)); // 1-3 segundos

            boolean paymentSuccess = false; // random.nextInt(100) < 60;

            if(paymentSuccess) {
                log.info("[PAYMENT] Pago procesado exitosamente para enrollment ID: {}", event.getEnrollmentId());

                // Generas un codigo de transaccion
                String transactionId = "tx-" + UUID.randomUUID();

                // Generar el evento
                PaymentProcessedEvent processedEvent = new PaymentProcessedEvent(
                        event.getEnrollmentId(),
                        transactionId,
                        event.getAmount(),
                        LocalDateTime.now());

                kafkaEventPublisher.publish(processedEvent);

                log.info("[PAYMENT] Pago procesado exitosamente");

            } else {
                log.warn("❌ [PAYMENT] El pago falló para enrollment ID: {}", event.getEnrollmentId());

                // TO DO

                // Genera el event del error

                PaymentFailedEvent failedEvent = new PaymentFailedEvent(
                        event.getEnrollmentId(),
                        "PAYMENT_DECLINED",
                        "El pago fue rechazado por el proveedor, saldo insuficiente.",
                        LocalDateTime.now()
                );


                // Publica el evento fallido
                this.kafkaEventPublisher.publish(failedEvent);

                log.warn("📨 [PAYMENT] Evento PaymentFailed publicado");

            }


        } catch (InterruptedException e) {
            log.error("[PAYMENT] Error procesando pago", e);
        }



    }
}
