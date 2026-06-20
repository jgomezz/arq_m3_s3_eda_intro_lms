package pe.edu.tecsup.lms.courses.application;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.tecsup.lms.courses.domain.event.CoursePublishedEvent;
import pe.edu.tecsup.lms.courses.domain.exception.CourseNotFoundException;
import pe.edu.tecsup.lms.courses.domain.model.Course;
import pe.edu.tecsup.lms.courses.domain.repository.CourseRepository;
import pe.edu.tecsup.lms.shared.domain.event.EventPublisher;
import pe.edu.tecsup.lms.shared.domain.event.RabbitMQEventPublisher;

import static pe.edu.tecsup.lms.shared.infrastructure.config.RabbitMQConfig.COURSE_CREATED_ROUTING_KEY;
import static pe.edu.tecsup.lms.shared.infrastructure.config.RabbitMQConfig.COURSE_PUBLISHED_ROUTING_KEY;

@Slf4j
@RequiredArgsConstructor
public class PublishCourseUseCaseImpl implements PublishCourseUseCase {

    private final CourseRepository repository;

    //private final EventPublisher eventPublisher;
    private final RabbitMQEventPublisher eventPublisher; // Nueva linea

    @Override
    @Transactional
    public Course publishCourse(Long courseId) {
        Course course = repository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        course.publish();
        Course saved = repository.save(course);

        log.info("Course published: {}", saved.getId());

        // Crear el eventp
        CoursePublishedEvent event
                = new CoursePublishedEvent(
                                            saved.getId().toString(),
                                            saved.getTitle()
                                            );

        // Publicar el evento en RabbitMQ
        this.eventPublisher.publish(COURSE_PUBLISHED_ROUTING_KEY, event);
        return saved;
    }
}
