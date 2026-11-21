package com.hotelboutique.reservas.kafka;

import com.hotelboutique.reservas.service.OcupacionService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OcupacionConsumerTest {

    @Mock private OcupacionService ocupacionService;
    @InjectMocks private OcupacionConsumer consumer;

    private static ConsumerRecord<String, ReservaStreamEvent> registro(ReservaStreamEvent evento) {
        return new ConsumerRecord<>("hotel.reservas.eventos", 1, 42L, "7", evento);
    }

    @Test
    void eventoValidoSeEntregaAlServicio() {
        ReservaStreamEvent evento = new ReservaStreamEvent("e-1", "reserva.checkin", 10L, "ana@mail.com", 7L,
                null, null, "CHECK_IN", Instant.now());

        consumer.onReservaEvento(registro(evento));

        verify(ocupacionService).aplicar(evento);
    }

    @Test
    void eventoSinTipoSeDescartaSinProcesar() {
        ReservaStreamEvent invalido = new ReservaStreamEvent("e-2", null, 10L, "ana@mail.com", 7L,
                null, null, null, Instant.now());

        consumer.onReservaEvento(registro(invalido));

        verify(ocupacionService, never()).aplicar(any());
    }

    @Test
    void valorNuloSeDescartaSinProcesar() {
        consumer.onReservaEvento(registro(null));

        verify(ocupacionService, never()).aplicar(any());
    }
}
