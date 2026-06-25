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


}
