## Implementar un sistema de comentarios y rating para los cursos 

### Requisitos Funcionales

-  Estudiantes dejan comentarios de un curso
-  Estudiante califican el curso con una puntuación de 1 a 5
-  Pueden editar los comentarios y ratings

### Requisitos Técnicos
-  Usar Event Sourcing para manejar comentarios y ratings

### Modulo a Implementar : Comentarios 
 
 - CourseComment --> Agregate
 - Eventos
     - CommentAddedEvent
     - CommentEditedEvent
 - CommentCommandHandler
 - MemoryEventStore ( ya existe en el  proyecto)