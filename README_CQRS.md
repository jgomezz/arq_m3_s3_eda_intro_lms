## Implementacion de CQRS ; Enrollment

### Diagrama de Secuencia

<img src="images/cqrs_sequence_diagram.png" />



1.- Crear el modelo de solo lectura, el repositorio y el projection

Localización:

<img src="images/cqrs_step_1.png" alt="CQRS" />

- **EnrollmentReadModel.java**

```java
package pe.edu.tecsup.lms.enrollments.application.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class EnrollmentReadModel {

    private final String enrollmentId;
    private final String studentId;
    private final String courseId;

    // Data desnormalizada
    private final String studentName;

    // Lesson
    private int progress;
    
}

```

- **EnrollmentQueryRepository.java**

```java
package pe.edu.tecsup.lms.enrollments.application.query;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class EnrollmentQueryRepository {

    private final Map<String, EnrollmentReadModel> readModels = new HashMap<>();

    // Update

    /**
     *
     * @param readModel
     */
    public void save(EnrollmentReadModel readModel) {
        this.readModels.put(readModel.getEnrollmentId(), readModel);
    }

    // Read

    /**
     *
     * @param enrollmentId
     * @return
     */
    public Optional<EnrollmentReadModel> findByEnrollmentId(String enrollmentId) {

        return Optional.ofNullable(this.readModels.get(enrollmentId));

    }


    /**
     *
     * @return
     */
    public List<EnrollmentReadModel> findAll() {

        return  new ArrayList<>(this.readModels.values());
    }

    
}

```

- **EnrollmentProjection.java**
```java
package pe.edu.tecsup.lms.enrollments.application.projection;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pe.edu.tecsup.lms.enrollments.application.query.EnrollmentQueryRepository;
import pe.edu.tecsup.lms.enrollments.application.query.EnrollmentReadModel;
import pe.edu.tecsup.lms.enrollments.domain.event.LessonCompletedEvent;
import pe.edu.tecsup.lms.enrollments.domain.event.StudentEnrolledEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnrollmentProjection {

    private final EnrollmentQueryRepository repository;

    /**
     *  Listening of StudentEnrolledEvent
     */
    @EventListener
    public void  onStudentEnrolled(StudentEnrolledEvent event) {

        log.info("EnrollmentProjection.onStudentEnrolled(event={})", event);


        var model = EnrollmentReadModel.builder()
                .enrollmentId(event.getEnrollmentId())
                .courseId(event.getCourseId())
                .studentId(event.getStudentId())
                .studentName(event.getStudentName())
                .progress(0)
                .build();

        this.repository.save(model);
    }

    /**
     *  Listening of LessonCompletedEvent
     */
    @EventListener
    public void  onLessonCompleted(LessonCompletedEvent event) {

        log.info("EnrollmentProjection.onLessonCompleted(event={})", event);

        // Buscar la INFO
        EnrollmentReadModel readModel
                = this.repository.findByEnrollmentId(event.getEnrollmentId()).orElseThrow();

        // Actualiza le progresso de la lección
        var newProgress = readModel.getProgress() + event.getNewProgressPercentage();

        // Guardar el objeto actualizado
        this.repository.save(readModel);

    }
    
}

```

- **EnrollmentProjectionTest.java**
```java
package pe.edu.tecsup.lms.enrollments.application.projection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.edu.tecsup.lms.enrollments.application.query.EnrollmentQueryRepository;
import pe.edu.tecsup.lms.enrollments.domain.event.StudentEnrolledEvent;

import static org.junit.jupiter.api.Assertions.*;

class EnrollmentProjectionTest {

    private EnrollmentProjection projection;
    private EnrollmentQueryRepository repository;

    @BeforeEach
    void init(){
        this.repository = new EnrollmentQueryRepository();
        this.projection = new EnrollmentProjection(this.repository);
    }

    @Test
    void onStudentEnrolled() {

        StudentEnrolledEvent event
                = StudentEnrolledEvent.builder()
                .enrollmentId("enroll-123")
                .studentId("student-123")
                .studentName("student-name")
                .courseId("course-123")
                .build();

        this.projection.onStudentEnrolled(event);

        var readModelOpt = this.repository.findByEnrollmentId("enroll-123");

        assertTrue(readModelOpt.isPresent());

        var readModel = readModelOpt.get();

        assertEquals("enroll-123", readModel.getEnrollmentId());
        assertEquals("student-123", readModel.getStudentId());
        assertEquals("student-name", readModel.getStudentName());

    }

    @Test
    void onLessonCompleted() {
    }
}
```
