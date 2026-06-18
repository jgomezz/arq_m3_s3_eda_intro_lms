package pe.edu.tecsup.lms.notifications.application.eventhandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pe.edu.tecsup.lms.courses.domain.event.CourseCreatedEvent;
import pe.edu.tecsup.lms.courses.domain.model.Course;
import pe.edu.tecsup.lms.shared.infrastructure.config.RabbitMQConfig;

/**
 * Es el consumidor de eventos
 */
@Slf4j
@Component
public class CourseEventHandler {

    //@EventListener
    @RabbitListener(queues = RabbitMQConfig.COURSE_QUEUE)
    public void handleCourseCreated(CourseCreatedEvent event)
    {
        log.info("[RabbitMQ] Course created event received: {}", event);


    }

}
