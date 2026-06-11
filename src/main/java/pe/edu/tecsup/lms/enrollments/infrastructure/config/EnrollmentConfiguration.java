package pe.edu.tecsup.lms.enrollments.infrastructure.config;

import pe.edu.tecsup.lms.enrollments.application.command.EnrollmentCommandHandler;
import pe.edu.tecsup.lms.shared.infrastructure.eventsourcing.MemoryEventStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnrollmentConfiguration {

    @Bean
    public EnrollmentCommandHandler enrollmentCommandHandler(MemoryEventStore eventStore) {
        return new EnrollmentCommandHandler(eventStore);
    }
}