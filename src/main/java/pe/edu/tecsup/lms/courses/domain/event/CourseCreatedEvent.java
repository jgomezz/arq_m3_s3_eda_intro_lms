package pe.edu.tecsup.lms.courses.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pe.edu.tecsup.lms.shared.domain.event.DomainEvent;

//@AllArgsConstructor
@Getter
@ToString
//@NoArgsConstructor(force = true)
public class CourseCreatedEvent extends DomainEvent {

    private final  String courseId;
    private final String title;
    private final  String instructor;

    public CourseCreatedEvent(final String courseId, final String title, final String instructor) {
        this.courseId = courseId;
        this.title = title;
        this.instructor = instructor;
    }

    public CourseCreatedEvent() {
        this.courseId = null;
        this.title = null;
        this.instructor = null;
    }


    @Override
    public String getKey() {       // SOBREESCRIBIR EL METODO
        return this.courseId;
    }

}
