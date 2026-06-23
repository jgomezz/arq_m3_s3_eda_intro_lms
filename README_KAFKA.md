# MIGRACIÓN A KAFKA

<img src="images/kafka_class.png" alt="Kafka" />


## **I.- Creación del servidor de Kafka**

- Creación del docker-compose.yml

```yaml

services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    container_name: zookeeper
    ports:
      - "2181:2181"
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    volumes:
      - zookeeper-data:/var/lib/zookeeper/data
      - zookeeper-logs:/var/lib/zookeeper/log

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    container_name: kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: 'zookeeper:2181'
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: 'true'
    volumes:
      - kafka-data:/var/lib/kafka/data

  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    container_name: kafka-ui
    depends_on:
      - kafka
    ports:
      - "8090:8080"
    environment:
      KAFKA_CLUSTERS_0_NAME: local
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:29092

volumes:
  zookeeper-data:
  zookeeper-logs:
  kafka-data:

```
Se debe ejecutar el docker-compose.yml con el siguiente comando:

```bash
docker-compose up -d        
```
El servidor se puede visualizar en http://localhost:8090

## **II.- Configuración de una aplicación de Spring Boot para usar Kafka**


1.- Agregar las dependencias en el pom.xml

```xml
    <!-- Spring for Apache Kafka -->
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka-test</artifactId>
        <scope>test</scope>
    </dependency>
```

2.- Configurar las propiedades de Kafka en application.properties

```properties

# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092

# Kafka Serializer Configuration
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer

# Kafka Deserializer Configuration
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer

# JSON Configuration
spring.kafka.consumer.properties.spring.json.trusted.packages=*

```

3.- Configurar los tópicos de Kafka en una clase de configuración

KafkaConfig.java
```java
package pe.edu.tecsup.lms.shared.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@Configuration
public class KafkaConfig {

    // Set TOPICS
    public static final String  COURSE_EVENT_TOPIC = "course.events";

    // Set QUEUES/PARTITIONS

    /**
     *  Topic de eventos del curso
     * @return
     */
    @Bean
    public NewTopic courseEventTopic() {

        return new NewTopic(COURSE_EVENT_TOPIC,  // topic
                3,   // Nro. particiones
                (short) 1  // Nro. de replicas
        );
    }

}

```

4.- Implementar un Productor de Eventos con Kafka

- Modificar la clase DomainEvent para agregar el método getKey()

DomainEvent.java

```java

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class DomainEvent {

    private final String eventId;

    private final String eventType;

    private final LocalDateTime ocurredOn;

    public DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = this.getClass().getSimpleName();
        this.ocurredOn = LocalDateTime.now();
    }
    
    /**
     * Obtener la clave del evento para particionamiento en Kafka
     * @return
     */
    public String getKey() {
        throw new RuntimeException("Method getKey() not implemented");
    }

}

```
- Se requiere modificar la clase CourseCreatedEvent para soportar la serialización con JSON ( Agregar el constructor sin argumentos)    y sorbreescribir el metodo getKey()

CourseCreatedEvent.java

```java
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

```
- Se requiere crear el publicador de eventos para Kafka

KafkaEventPublisher.java

```java
package pe.edu.tecsup.lms.shared.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pe.edu.tecsup.lms.shared.domain.event.DomainEvent;
import pe.edu.tecsup.lms.shared.infrastructure.config.KafkaConfig;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {

    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;

    public void publish(DomainEvent event) {

        log.info("Publishing {}", event);

        String key = event.getKey();

        this.kafkaTemplate.send(
                KafkaConfig.COURSE_EVENT_TOPIC,
                key,
                event);

    }

    // La key sirve para identificar a que particion va el mensaje
    // HASH(key) % N_PARTICIONES = particion
    

}
```
- Adaptar la clase CreateCourseUseCaseImpl.java

```java
package pe.edu.tecsup.lms.courses.application;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.edu.tecsup.lms.courses.domain.event.CourseCreatedEvent;
import pe.edu.tecsup.lms.courses.domain.model.Course;
import pe.edu.tecsup.lms.courses.domain.repository.CourseRepository;
import pe.edu.tecsup.lms.shared.domain.event.EventPublisher;
import pe.edu.tecsup.lms.shared.infrastructure.event.KafkaEventPublisher;

@Slf4j
@RequiredArgsConstructor
public class CreateCourseUseCaseImpl implements CreateCourseUseCase {

    private final CourseRepository repository;

    //private final EventPublisher eventPublisher;
    private final KafkaEventPublisher eventPublisher;


    @Override
    public Course createCourse(String title, String description, String instructor) {
        Course course = Course.create(title, description, instructor);
        Course saved = repository.save(course);
        log.info("Course created: {}", saved.getId());

        // Crear el evento
        CourseCreatedEvent event =
                new CourseCreatedEvent(
                        saved.getId().toString(),
                        saved.getTitle(),
                        saved.getInstructor());

        // Publicar el evento
        this.eventPublisher.publish(event);


        return saved;
    }
}


```

- Adaptar la clase BeanConfiguration.java

```java
package pe.edu.tecsup.lms.courses.infrastructure.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.edu.tecsup.lms.courses.application.CreateCourseUseCase;

import pe.edu.tecsup.lms.courses.application.CreateCourseUseCaseImpl;
import pe.edu.tecsup.lms.courses.application.PublishCourseUseCase;
import pe.edu.tecsup.lms.courses.application.PublishCourseUseCaseImpl;

import pe.edu.tecsup.lms.courses.domain.repository.CourseRepository;
import pe.edu.tecsup.lms.shared.domain.event.EventPublisher;
import pe.edu.tecsup.lms.shared.infrastructure.event.KafkaEventPublisher;

/**
 * CONFIGURACIÓN DE BEANS
 *
 * Registra los Use Cases (impls) detrás de su interfaz (input port).
 * El controller depende de la interfaz, no de la implementación.
 */
@Configuration
public class BeanConfiguration {

    @Bean
    public CreateCourseUseCase createCourseUseCase(CourseRepository repository, KafkaEventPublisher eventPublisher) {

        return new CreateCourseUseCaseImpl(repository, eventPublisher);

    }

    @Bean
    public PublishCourseUseCase publishCourseUseCase(CourseRepository repository, EventPublisher eventPublisher) {

        return new PublishCourseUseCaseImpl(repository, eventPublisher);

    }
}

```

5.- Realizar pruebas de creación de curso


6.- Implementar un Consumidor de Eventos con Kafka ( subdomain notifications)

CourseEventHandler.java
```java
package pe.edu.tecsup.lms.notifications.application.eventhandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.edu.tecsup.lms.courses.domain.event.CourseCreatedEvent;
import pe.edu.tecsup.lms.shared.domain.event.DomainEvent;
import pe.edu.tecsup.lms.shared.infrastructure.config.KafkaConfig;

/**
 * Es el consumidor de eventos
 */
@Slf4j
@Component
public class CourseEventHandler {

    //@EventListener
    @KafkaListener(
         topics = KafkaConfig.COURSE_EVENT_TOPIC,      // Topico que va a escuchando
         groupId = "course-notifications-group"         // Grupo de consumidores
    )
     public void handleCourseEvents(DomainEvent event) {
        if (event instanceof CourseCreatedEvent) {
            this.handleCourseCreated((CourseCreatedEvent) event);
        } else {
            throw new RuntimeException("Invalid event type " + event.getClass());
        }
    }

    public void handleCourseCreated(CourseCreatedEvent event)
    {
        log.info("[Kafka] Course created event received: {}", event);

    }

}


```


