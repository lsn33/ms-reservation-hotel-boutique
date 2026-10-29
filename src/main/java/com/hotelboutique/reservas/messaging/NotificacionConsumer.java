package com.hotelboutique.reservas.messaging;

import com.hotelboutique.reservas.event.ReservaEvento;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificacionConsumer {

    @RabbitListener(queues = "${hotel.messaging.queues.notificaciones}")
    public void onNotificacion(ReservaEvento evento) {
        try {
            // Aqui iria el envio real (correo, push, etc.). Por ahora se registra en el log.
            log.info("Notificacion [{}] para {} (reserva {}, checkin {}, checkout {})",
                    evento.tipo(), evento.usuarioEmail(), evento.reservaId(),
                    evento.fechaCheckin(), evento.fechaCheckout());
        } catch (RuntimeException ex) {
            log.error("Error al procesar notificacion de la reserva {}, se envia a DLQ: {}",
                    evento.reservaId(), ex.getMessage(), ex);
            throw new AmqpRejectAndDontRequeueException("Error al procesar notificacion", ex);
        }
    }
}
