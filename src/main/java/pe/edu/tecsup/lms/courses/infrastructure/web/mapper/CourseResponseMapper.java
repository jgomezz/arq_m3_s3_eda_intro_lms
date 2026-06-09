package pe.edu.tecsup.lms.courses.infrastructure.web.mapper;

import org.mapstruct.Mapper;
import pe.edu.tecsup.lms.courses.domain.model.Course;
import pe.edu.tecsup.lms.courses.infrastructure.web.dto.CourseResponse;

@Mapper(componentModel = "spring")
public interface CourseResponseMapper {

    CourseResponse toResponse(Course course);
}
