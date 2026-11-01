package com.hotelboutique.reservas.messaging;

import com.hotelboutique.reservas.event.ReservaEvento;
import com.hotelboutique.reservas.service.TicketService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketConsumer {

    private final TicketService ticketService;

    @RabbitListener(queues = "${hotel.messaging.queues.tickets}")
    public void onReservaCreada(ReservaEvento evento, Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        if (!datosValidos(evento)) {
            log.error("Evento invalido para ticket (reserva {}), NACK sin requeue",
                    evento == null ? null : evento.reservaId());
            channel.basicNack(tag, false, false);
            return;
        }

        ticketService.emitirTicket(evento);
        channel.basicAck(tag, false);
    }

    private boolean datosValidos(ReservaEvento evento) {
        return evento != null
                && evento.reservaId() != null
                && evento.usuarioEmail() != null
                && !evento.usuarioEmail().isBlank();
    }
}
