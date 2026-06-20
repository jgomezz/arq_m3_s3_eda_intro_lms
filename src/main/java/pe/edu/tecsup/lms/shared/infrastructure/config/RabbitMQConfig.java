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
    @Bean
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
