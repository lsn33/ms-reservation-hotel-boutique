package com.hotelboutique.reservas.messaging;

import com.hotelboutique.reservas.event.ReservaEvento;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;

class NotificacionConsumerTest {

    @Test
    void registraNotificacionSinLanzarExcepcion() {
        NotificacionConsumer consumer = new NotificacionConsumer();
        ReservaEvento evento = new ReservaEvento(
                "reserva.creada", 1L, "prueba@hotel.cl",
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3));

        assertThatCode(() -> consumer.onNotificacion(evento)).doesNotThrowAnyException();
    }
}
