package com.hotelboutique.reservas.messaging;

import com.hotelboutique.reservas.event.ReservaEvento;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.mockito.Mockito.*;

class NotificacionConsumerTest {

    @Test
    void registraNotificacionYConfirmaConAck() throws Exception {
        Channel channel = mock(Channel.class);
        ReservaEvento evento = new ReservaEvento("reserva.creada", 1L, "prueba@hotel.cl",
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3));

        new NotificacionConsumer().onNotificacion(evento, channel, 2L);

        verify(channel).basicAck(2L, false);
    }
}
