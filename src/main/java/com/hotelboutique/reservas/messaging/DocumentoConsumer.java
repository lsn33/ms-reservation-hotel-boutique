package com.hotelboutique.reservas.messaging;

import com.hotelboutique.reservas.event.ReservaEvento;
import com.hotelboutique.reservas.service.DocumentoService;
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
public class DocumentoConsumer {

    private final DocumentoService documentoService;

    @RabbitListener(queues = "${hotel.messaging.queues.documentos}")
    public void onComprobante(ReservaEvento evento, Channel channel,
                              @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        if (!datosValidos(evento)) {
            log.error("Evento invalido para comprobante (reserva {}), NACK sin requeue",
                    evento == null ? null : evento.reservaId());
            channel.basicNack(tag, false, false);
            return;
        }

        documentoService.generarComprobante(evento);
        channel.basicAck(tag, false);
    }

    private boolean datosValidos(ReservaEvento evento) {
        return evento != null
                && evento.reservaId() != null
                && evento.usuarioEmail() != null
                && !evento.usuarioEmail().isBlank();
    }
}
