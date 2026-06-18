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
        return new Queue(COURSE_QUEUE, true);
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
