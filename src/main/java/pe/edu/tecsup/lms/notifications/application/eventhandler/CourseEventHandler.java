package pe.edu.tecsup.lms.notifications.application.eventhandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.edu.tecsup.lms.courses.domain.event.CourseCreatedEvent;
import pe.edu.tecsup.lms.shared.domain.event.DomainEvent;
import pe.edu.tecsup.lms.shared.infrastructure.config.KafkaConfig;

/**
 * Es el consumidor de eventos
 */
@Slf4j
@Component
public class CourseEventHandler {

    //@EventListener
    @KafkaListener(
         topics = KafkaConfig.COURSE_EVENT_TOPIC,      // Topico que va a escuchando
         groupId = "course-notifications-group"         // Grupo de consumidores
    )
     public void handleCourseEvents(DomainEvent event) {
        if (event instanceof CourseCreatedEvent) {
            this.handleCourseCreated((CourseCreatedEvent) event);
        } else {
            throw new RuntimeException("Invalid event type " + event.getClass());
        }
    }

    public void handleCourseCreated(CourseCreatedEvent event)
    {
        log.info("[Kafka] Course created event received: {}", event);

    }

}
