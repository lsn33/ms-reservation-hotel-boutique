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

    private static final String ROUTING_COMPROBANTE = "comprobante";

    private final RabbitTemplate rabbitTemplate;

    @Value("${hotel.messaging.exchanges.eventos}")
    private String exchangeEventos;

    @Value("${hotel.messaging.exchanges.documentos}")
    private String exchangeDocumentos;

    public void publicar(ReservaEvento evento) {
        try {
            rabbitTemplate.convertAndSend(exchangeEventos, evento.tipo(), evento);
            log.info("Evento publicado: {} reserva={}", evento.tipo(), evento.reservaId());
        } catch (AmqpException ex) {
            log.error("No se pudo publicar el evento {} de la reserva {}: {}",
                    evento.tipo(), evento.reservaId(), ex.getMessage(), ex);
        }
    }

    public void publicarComprobante(ReservaEvento evento) {
        try {
            rabbitTemplate.convertAndSend(exchangeDocumentos, ROUTING_COMPROBANTE, evento);
            log.info("Comprobante solicitado para la reserva {}", evento.reservaId());
        } catch (AmqpException ex) {
            log.error("No se pudo solicitar el comprobante de la reserva {}: {}",
                    evento.reservaId(), ex.getMessage(), ex);
        }
    }
}
