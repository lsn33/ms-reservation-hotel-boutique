package com.hotelboutique.reservas.messaging;

import com.hotelboutique.reservas.event.ReservaEvento;
import com.hotelboutique.reservas.service.DocumentoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentoConsumerTest {

    @Mock private DocumentoService documentoService;
    @InjectMocks private DocumentoConsumer consumer;

    private final ReservaEvento evento = new ReservaEvento(
            "reserva.checkout", 1L, "prueba@hotel.cl",
            LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3));

    @Test
    void procesaElEventoGenerandoComprobante() {
        consumer.onComprobante(evento);

        verify(documentoService).generarComprobante(evento);
    }

    @Test
    void errorRechazaSinRequeueParaQueVayaADlq() {
        doThrow(new RuntimeException("falla de base de datos"))
                .when(documentoService).generarComprobante(evento);

        assertThatThrownBy(() -> consumer.onComprobante(evento))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);
    }
}
