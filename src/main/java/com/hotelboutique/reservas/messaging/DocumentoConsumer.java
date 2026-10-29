package com.hotelboutique.reservas.messaging;

import com.hotelboutique.reservas.event.ReservaEvento;
import com.hotelboutique.reservas.service.DocumentoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentoConsumer {

    private final DocumentoService documentoService;

    @RabbitListener(queues = "${hotel.messaging.queues.documentos}")
    public void onComprobante(ReservaEvento evento) {
        try {
            documentoService.generarComprobante(evento);
        } catch (RuntimeException ex) {
            log.error("Error al generar comprobante de la reserva {}, se envia a DLQ: {}",
                    evento.reservaId(), ex.getMessage(), ex);
            throw new AmqpRejectAndDontRequeueException("Error al generar comprobante", ex);
        }
    }
}
