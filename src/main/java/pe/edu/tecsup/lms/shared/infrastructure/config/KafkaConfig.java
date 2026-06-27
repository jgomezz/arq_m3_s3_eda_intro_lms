package pe.edu.tecsup.lms.shared.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@EnableKafka
@Configuration
public class KafkaConfig {

    // Set TOPICS
    public static final String COURSE_EVENTS_TOPIC = "course.events";


    // DLQ
    public static final String DLQ_COURSE_EVENTS_TOPIC = "dlq.course.events";  // ✅ DLQ Topic


    // SAGA
    public static final String ENROLLMENT_REQUEST_TOPIC = "enrollment.requested";

    public static final String PAYMENT_PROCESSED_TOPIC = "payment.processed";   // AGREGAR

    public static final String PAYMENT_FAILED_TOPIC = "payment.failed";  // AGREGAR


    // Set QUEUES/PARTITIONS

    /**
     *  Topic de eventos del curso
     * @return
     */
    @Bean
    public NewTopic courseEventTopic() {

        return new NewTopic(COURSE_EVENTS_TOPIC,  // topic
                3,   // Nro. particiones
                (short) 1  // Nro. de replicas
        );
    }

    // DLQ
    @Bean
    public NewTopic dlqCourseEventsTopic() {
        return TopicBuilder.name(DLQ_COURSE_EVENTS_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    // SAGA
    @Bean
    public NewTopic enrollmentRequestedTopic() {
        return TopicBuilder
                .name(ENROLLMENT_REQUEST_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentProcessedTopic() {
        return TopicBuilder
                .name(PAYMENT_PROCESSED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }


    // NUEVO BEAN
    @Bean
    public NewTopic paymentFailedTopic() {
        return TopicBuilder
                .name(PAYMENT_FAILED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

}
