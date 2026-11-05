package com.hotelboutique.reservas.messaging;

import com.hotelboutique.reservas.event.ReservaEvento;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

/** Convierte el cuerpo crudo del mensaje; devuelve null si no es un JSON valido. */
@Slf4j
final class ReservaEventoParser {

    private static final JsonMapper MAPPER = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    private ReservaEventoParser() {
    }

    static ReservaEvento parsear(Message mensaje) {
        try {
            return MAPPER.readValue(mensaje.getBody(), ReservaEvento.class);
        } catch (Exception e) {
            log.error("Mensaje no convertible a ReservaEvento: {}", e.getMessage());
            return null;
        }
    }
}