package com.hotelboutique.reservas.messaging;

import com.hotelboutique.reservas.event.ReservaEvento;
import com.hotelboutique.reservas.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketConsumer {

    private final TicketService ticketService;

    @RabbitListener(queues = "${hotel.messaging.queues.tickets}")
    public void onReservaCreada(ReservaEvento evento) {
        try {
            ticketService.emitirTicket(evento);
        } catch (RuntimeException ex) {
            log.error("Error al emitir ticket de la reserva {}, se envia a DLQ: {}",
                    evento.reservaId(), ex.getMessage(), ex);
            throw new AmqpRejectAndDontRequeueException("Error al emitir ticket", ex);
        }
    }
}
