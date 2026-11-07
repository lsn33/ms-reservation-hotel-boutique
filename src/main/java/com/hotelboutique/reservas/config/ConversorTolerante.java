package com.hotelboutique.reservas.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;

public class ConversorTolerante extends JacksonJsonMessageConverter {

    public ConversorTolerante(String... paquetesConfiables) {
        super(paquetesConfiables);
    }

    @Override
    public Object fromMessage(Message message) {
        try {
            return super.fromMessage(message);
        } catch (Exception e) {
            return message.getBody();
        }
    }
}