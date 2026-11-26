package com.hotelboutique.reservas.config;

import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMessagingConfig {

    @Bean
    public MessageConverter messageConverter() {
        return new ConversorTolerante("com.hotelboutique.reservas.event");
    }
}