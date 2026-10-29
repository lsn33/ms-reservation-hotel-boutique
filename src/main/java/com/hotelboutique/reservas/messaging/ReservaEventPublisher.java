package com.hotelboutique.reservas.messaging;

import com.hotelboutique.reservas.event.ReservaEvento;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservaEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${hotel.messaging.exchanges.eventos}")
    private String exchangeEventos;

    public void publicar(ReservaEvento evento) {
        try {
            rabbitTemplate.convertAndSend(exchangeEventos, evento.tipo(), evento);
            log.info("Evento publicado: {} reserva={}", evento.tipo(), evento.reservaId());
        } catch (AmqpException ex) {
            log.error("No se pudo publicar el evento {} de la reserva {}: {}",
                    evento.tipo(), evento.reservaId(), ex.getMessage(), ex);
        }
    }
}
