package pe.edu.tecsup.lms.courses.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pe.edu.tecsup.lms.shared.domain.event.DomainEvent;

@AllArgsConstructor
@Getter
@ToString
@NoArgsConstructor(force = true)
public class CoursePublishedEvent extends DomainEvent {

    private final String courseId;
    private final String title;

    @Override
    public String getKey() {       // SOBREESCRIBIR EL METODO
        return this.courseId;
    }


}
