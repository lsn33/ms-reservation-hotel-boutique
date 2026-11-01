package com.hotelboutique.reservas.messaging;

import com.hotelboutique.reservas.event.ReservaEvento;
import com.hotelboutique.reservas.service.TicketService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketConsumerTest {

    @Mock private TicketService ticketService;
    @Mock private Channel channel;
    @InjectMocks private TicketConsumer consumer;

    private ReservaEvento evento(String usuarioEmail) {
        return new ReservaEvento("reserva.creada", 1L, usuarioEmail,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3));
    }

    @Test
    void procesaYConfirmaConAck() throws Exception {
        ReservaEvento evento = evento("prueba@hotel.cl");

        consumer.onReservaCreada(evento, channel, 1L);

        verify(ticketService).emitirTicket(evento);
        verify(channel).basicAck(1L, false);
    }

    @Test
    void datosInvalidosHacenNackSinRequeue() throws Exception {
        consumer.onReservaCreada(evento(null), channel, 7L);

        verify(channel).basicNack(7L, false, false);
        verifyNoInteractions(ticketService);
    }

    @Test
    void errorRecuperableSePropagaSinConfirmar() throws Exception {
        ReservaEvento evento = evento("prueba@hotel.cl");
        doThrow(new RuntimeException("base de datos no disponible"))
                .when(ticketService).emitirTicket(evento);

        assertThatThrownBy(() -> consumer.onReservaCreada(evento, channel, 3L))
                .isInstanceOf(RuntimeException.class);

        verify(channel, never()).basicAck(anyLong(), anyBoolean());
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }
}
