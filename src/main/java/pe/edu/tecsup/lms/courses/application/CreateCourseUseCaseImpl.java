package pe.edu.tecsup.lms.courses.application;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.edu.tecsup.lms.courses.domain.event.CourseCreatedEvent;
import pe.edu.tecsup.lms.courses.domain.model.Course;
import pe.edu.tecsup.lms.courses.domain.repository.CourseRepository;
import pe.edu.tecsup.lms.shared.domain.event.EventPublisher;
import pe.edu.tecsup.lms.shared.domain.event.RabbitMQEventPublisher;

import static pe.edu.tecsup.lms.shared.infrastructure.config.RabbitMQConfig.COURSE_CREATED_ROUTING_KEY;

@Slf4j
@RequiredArgsConstructor
public class CreateCourseUseCaseImpl implements CreateCourseUseCase {

    private final CourseRepository repository;

    //private final EventPublisher eventPublisher;
    private final RabbitMQEventPublisher eventPublisher; // Nueva linea


    @Override
    public Course createCourse(String title, String description, String instructor) {

        // Crear el curso
        Course course = Course.create(title, description, instructor);
        Course saved = repository.save(course);
        log.info("Course created: {}", saved.getId());

        // Crear el evento de creacion del curso
        CourseCreatedEvent event =
                new CourseCreatedEvent(
                        saved.getId().toString(),
                        saved.getTitle(),
                        saved.getInstructor());

        // Publicar el evento en RabbitMQ
        this.eventPublisher.publish(COURSE_CREATED_ROUTING_KEY, event);

        return saved;
    }
}
