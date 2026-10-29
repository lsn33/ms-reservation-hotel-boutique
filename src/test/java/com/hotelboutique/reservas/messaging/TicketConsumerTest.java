package com.hotelboutique.reservas.messaging;

import com.hotelboutique.reservas.event.ReservaEvento;
import com.hotelboutique.reservas.service.TicketService;
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
class TicketConsumerTest {

    @Mock private TicketService ticketService;
    @InjectMocks private TicketConsumer consumer;

    private final ReservaEvento evento = new ReservaEvento(
            "reserva.creada", 1L, "prueba@hotel.cl",
            LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3));

    @Test
    void procesaElEventoEmitiendoTicket() {
        consumer.onReservaCreada(evento);

        verify(ticketService).emitirTicket(evento);
    }

    @Test
    void errorRechazaSinRequeueParaQueVayaADlq() {
        doThrow(new RuntimeException("falla de base de datos"))
                .when(ticketService).emitirTicket(evento);

        assertThatThrownBy(() -> consumer.onReservaCreada(evento))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);
    }
}
