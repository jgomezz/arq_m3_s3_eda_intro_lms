# MIGRACIÓN A RABBITMQ

- Workflow

<img src="images/rabbitmq_workflow.png" alt="RabbitMQ" />

- Clases a modificar

<img src="images/rabbitmq_class.png" alt="RabbitMQ" />

## **I.- Creación del servidor de RabbitMQ**

1. Requisitos : Tener instalado Docker Desktop : https://www.docker.com/products/docker-desktop/


2. Crear el docker compose para RabbitMQ : docker-compose.yml

docker-compose.yml

```yaml
services:
  rabbitmq:
    image: rabbitmq:3-management
    container_name: lms-rabbitmq
    hostname: tecsup-lms-rabbitmq
    ports:
      - "5672:5672"       # Puerto para conexiones AMQP
      - "15672:15672"     # Puerto para la interfaz de administración
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: admin123
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
volumes:
  rabbitmq_data:
```
- Ejecutar el docker compose en la carpeta donde se encuentra el archivo docker-compose.yml

```bash

docker-compose up -d

```
- Acceder al enlace http://localhost:15672
    - Usuario: admin
    - Clave: admin123

## **II.- Configuración de la aplicación web : dependencias y conexiones**

3.- Agregar dependencias en el pom.xml para RabbitMQ
```xml
<!-- Spring AMQP / RabbitMQ -->

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-amqp</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.amqp</groupId>
			<artifactId>spring-rabbit-test</artifactId>
			<scope>test</scope>
		</dependency>

```
4.- Configurar conexiôn al servidor de RabbitMQ 
```properties
# Soporte para RabbitMQ
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=admin
spring.rabbitmq.password=admin123

```

5.- Crear la configuración de RabbitMQ para el evento de creación de Cursos


RabbitMQConfig.java
```java
package pe.edu.tecsup.lms.shared.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 *  Exchange :
 *  Queuue :
 *  Routing Key
 */
@Configuration
public class RabbitMQConfig {

  public static final String EXCHANGE_NAME = "lms.exchange";

  public static final String COURSE_QUEUE = "lms.queue.course";

  public static final String COURSE_CREATED_ROUTING_KEY = "lms.queue.course.created";


  // Exchange
  @Bean
  public TopicExchange topicExchange() {
    return new TopicExchange(EXCHANGE_NAME);
  }


  // Queue
  @Bean
  public Queue courseQueue() {
    return new Queue(COURSE_QUEUE);
  }


  // Bindings
  @Bean
  public Binding courseBinding() {

    return BindingBuilder
            .bind(courseQueue()) // queue
            .to(topicExchange()) // exchange
            .with(COURSE_CREATED_ROUTING_KEY);

  }

  // Serializacion
  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }


}

```

## **III.- Configuración del publicador de eventos en RabbitMQb**

6.- Creación del publicador para RabbitMQ : **RabbitMQEventPublisher.java**

```java
package pe.edu.tecsup.lms.shared.domain.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static pe.edu.tecsup.lms.shared.infrastructure.config.RabbitMQConfig.EXCHANGE_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQEventPublisher {

  private final RabbitTemplate rabbitTemplate;

  /**
   *  Método que publica el evento en RabbitMQ
   * @param routingKey
   * @param event
   */
  public void publish(String routingKey, DomainEvent event) {
    log.info("Publishing event in RabbitMQ: {}", event);
    log.info("routingKey: {}", routingKey);

    this.rabbitTemplate.convertAndSend(
            EXCHANGE_NAME,
            routingKey,
            event,
            message -> {
              message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
              return message;}
    );
  }

}

```

7.- Adaptar el UseCase de creación de curso

Modificar la clase CreateCourseUseCaseImpl.java

```java
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

```

Modificar la clase BeanConfiguration.java

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
import pe.edu.tecsup.lms.shared.domain.event.RabbitMQEventPublisher;

/**
 * CONFIGURACIÓN DE BEANS
 *
 * Registra los Use Cases (impls) detrás de su interfaz (input port).
 * El controller depende de la interfaz, no de la implementación.
 */
@Configuration
public class BeanConfiguration {

    @Bean
//    public CreateCourseUseCase createCourseUseCase(CourseRepository repository, EventPublisher eventPublisher) {
    public CreateCourseUseCase createCourseUseCase(CourseRepository repository, RabbitMQEventPublisher eventPublisher) { // Cambio

        return new CreateCourseUseCaseImpl(repository, eventPublisher);

    }

    @Bean
    public PublishCourseUseCase publishCourseUseCase(CourseRepository repository, EventPublisher eventPublisher) {

        return new PublishCourseUseCaseImpl(repository, eventPublisher);

    }
}


```
8.- Realizar la creación de un curso y revisar en la consola del RabbitMQ que se ha recibido el mensaje

**NOTA** : Para que un mensaje sea persistente , se debe cumplir lo siguiente :
- 1.- Exchange debe ser DURABLE
- 2.- Queue debe ser DURABLE
- 3.- El mensaje debe ser PERSISTENTE

## **IV.- Configuración del Consumidor de eventos en RabbitMQb**


9.- Modificar el Consumidor de Notificación de creación de curso

<img src="images/rabbitmq_consumer_class.png" alt="RabbitMQ" />

CourseEventHandler.java

```java
package pe.edu.tecsup.lms.notifications.application.eventhandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pe.edu.tecsup.lms.courses.domain.event.CourseCreatedEvent;
import pe.edu.tecsup.lms.courses.domain.model.Course;
import pe.edu.tecsup.lms.shared.infrastructure.config.RabbitMQConfig;

/**
* Es el consumidor de eventos
  */
  @Slf4j
  @Component
  public class CourseEventHandler {

  //@EventListener
  @RabbitListener(queues = RabbitMQConfig.COURSE_QUEUE)
  public void handleCourseCreated(CourseCreatedEvent event)
  {
  log.info("[RabbitMQ] Course created event received: {}", event);


    }

}
```

## EJERCICIO DE MIGRACIÓN A RABBITMQ

- Migrar a RabbitMQ el evento de Publicación de Cursos.

## EJERCICIO DE CONSUMIDOR

- Crear un consumidor de RabbitMQ que escuche las Publicaciones de cursos.

## **V.- Configuración DLQ en RabbitMQ**

9.- Definir los intentos máximos de reintento y el tiempo de espera entre reintentos en application.properties

```properties

# Retry
spring.rabbitmq.listener.simple.retry.enabled=true
spring.rabbitmq.listener.simple.retry.max-attempts=3
spring.rabbitmq.listener.simple.retry.initial-interval=1000
spring.rabbitmq.listener.simple.retry.multiplier=2.0

# DLQ
spring.rabbitmq.listener.simple.default-requeue-rejected=false

```

10. Modificar la configuración de la clase RabbitMQConfig.java para agregar la cola del DLQ

  
```
                      RK_PAYMENT_DLQ
EXCHANGE_DLQ_NAME  ----------------->   PAYMENT_DLQ  

```
```java
package pe.edu.tecsup.lms.shared.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 *
 *  Exchange :
 *  Queuue :
 *  Routing Key
 */
@Configuration
public class RabbitMQConfig {

    // Exchanges
    public static final String EXCHANGE_NAME = "lms.exchange";
    public static final String EXCHANGE_DLQ_NAME = "lms.exchange.dlq";


    // Queues
    public static final String COURSE_QUEUE = "lms.queue.course";
    public static final String PAYMENT_QUEUE = "lms.queue.payment";


    // Queues
    public static final String PAYMENT_DLQ_QUEUE = "lms.queue.payment.dlq";


    // Routing Keys
    public static final String COURSE_CREATED_ROUTING_KEY = "rk.course.created";
    public static final String COURSE_PUBLISHED_ROUTING_KEY = "rk.course.published";


    // Exchange
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    // Exchange DLQ
    @Bean
    public DirectExchange directDLQExchange() {
        return new DirectExchange(EXCHANGE_DLQ_NAME, true, false);
    }


    // Course Queue
    @Bean
    public Queue courseQueue() {
        return new Queue(COURSE_QUEUE, true);
    }

    // Payment Queue
    @Bean
    public Queue paymentQueue() {

        Map<String, Object> params
                = Map.of(
                        "x-dead-letter-exchange", EXCHANGE_DLQ_NAME,
                        "x-dead-letter-routing-key", PAYMENT_DLQ_QUEUE
                    );

        return new Queue(PAYMENT_QUEUE, true, false, false,  params);
    }

    // Payment DLQ Queue
    public Queue paymentDLQQueue() {
        return new Queue(PAYMENT_DLQ_QUEUE, true);
    }

    // Bindings

    @Bean
    public Binding courseBinding() {

        return BindingBuilder
                .bind(courseQueue()) // queue
                .to(topicExchange()) // exchange
                .with(COURSE_CREATED_ROUTING_KEY);

    }

    @Bean
    public Binding paymentBinding() {

        return BindingBuilder
                .bind(paymentQueue()) // queue
                .to(topicExchange()) // exchange
                .with(COURSE_PUBLISHED_ROUTING_KEY);

    }

    // Binding DLQ
    @Bean
    public Binding paymentDLQBinding() {

        return BindingBuilder
                .bind(paymentDLQQueue()) // Queue DLQ
                .to(directDLQExchange()) // Exchange DLQ
                .with(PAYMENT_DLQ_QUEUE);   // Routing Key DLQ

    }


    // Serializacion
    @Bean
    public MessageConverter jsonMessageConverter() {
         return new Jackson2JsonMessageConverter();
    }
    
}

```

11.- Modificar el consumidor de DLQ : PaymentEventHandler.java

```java

package pe.edu.tecsup.lms.payment.application.eventhandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import pe.edu.tecsup.lms.courses.domain.event.CoursePublishedEvent;
import pe.edu.tecsup.lms.shared.infrastructure.config.RabbitMQConfig;
import pe.edu.tecsup.lms.shared.infrastructure.dlq.DeadLetterQueue;

import java.util.Random;

@Slf4j
@RequiredArgsConstructor  // Agregar constructor para inyección de dependencias
@Component
public class PaymentEventHandler {

    private final Random random = new Random();
    private final DeadLetterQueue dlq;  // Inyectar la DLQ

    @RabbitListener ( queues = RabbitMQConfig.PAYMENT_QUEUE)
    public void handleCoursePublished(CoursePublishedEvent event) throws InterruptedException {

        log.info("Processing payment ........ : {}", event);

        log.info("[{}] Processing payment ...", Thread.currentThread().getName());

        if (this.random.nextBoolean()) {
            log.error("Processing payment take longer times ........ : {}", event);
            throw new RuntimeException("Payment failed due to timeout");
        } else {
            log.info("Payment successfully processed");
        }

    }
    
}

```

12.- Retirar las dependencias de Spring Retry del pom.xml

```
		<!-- Spring Retry -->
		<dependency>
			<groupId>org.springframework.retry</groupId>
			<artifactId>spring-retry</artifactId>
			<version>2.0.4</version>
		</dependency>
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-aspects</artifactId>
		</dependency>
```