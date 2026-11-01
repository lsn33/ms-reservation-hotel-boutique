package com.hotelboutique.reservas.messaging;

import com.hotelboutique.reservas.event.ReservaEvento;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class NotificacionConsumer {

    @RabbitListener(queues = "${hotel.messaging.queues.notificaciones}")
    public void onNotificacion(ReservaEvento evento, Channel channel,
                               @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("Notificacion [{}] para {} (reserva {}, checkin {}, checkout {})",
                evento.tipo(), evento.usuarioEmail(), evento.reservaId(),
                evento.fechaCheckin(), evento.fechaCheckout());
        channel.basicAck(tag, false);
    }
}
