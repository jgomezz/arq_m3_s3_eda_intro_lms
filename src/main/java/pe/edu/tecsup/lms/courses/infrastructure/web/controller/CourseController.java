package pe.edu.tecsup.lms.courses.infrastructure.web.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.tecsup.lms.courses.application.CreateCourseUseCase;
import pe.edu.tecsup.lms.courses.application.PublishCourseUseCase;
import pe.edu.tecsup.lms.courses.domain.model.Course;
import pe.edu.tecsup.lms.courses.infrastructure.web.dto.CourseResponse;
import pe.edu.tecsup.lms.courses.infrastructure.web.dto.CreateCourseRequest;
import pe.edu.tecsup.lms.courses.infrastructure.web.mapper.CourseResponseMapper;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CreateCourseUseCase createCourseUseCase;
    private final PublishCourseUseCase publishCourseUseCase;
    private final CourseResponseMapper courseResponseMapper;

    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(@RequestBody CreateCourseRequest request) {
        Course course = createCourseUseCase.createCourse(
                request.getTitle(),
                request.getDescription(),
                request.getInstructor()
        );
        return ResponseEntity.ok(courseResponseMapper.toResponse(course));
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<CourseResponse> publishCourse(@PathVariable Long id) {
        Course course = publishCourseUseCase.publishCourse(id);
        return ResponseEntity.ok(courseResponseMapper.toResponse(course));
    }
}
