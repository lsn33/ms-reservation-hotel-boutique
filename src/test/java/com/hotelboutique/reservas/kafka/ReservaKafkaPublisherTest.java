package com.hotelboutique.reservas.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReservaKafkaPublisherTest {

    private static final String TOPICO = "hotel.reservas.eventos";

    private KafkaTemplate<String, Object> kafkaTemplate;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
    }

    private static ReservaStreamEvent evento(Long habitacionId) {
        return new ReservaStreamEvent("e-1", "reserva.checkin", 10L, "ana@mail.com", habitacionId,
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3), "CHECK_IN", Instant.now());
    }

    @Test
    void publicaConLaHabitacionComoClaveParaMantenerElOrden() {
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(CompletableFuture.completedFuture(null));
        ReservaStreamEvent evento = evento(7L);

        new ReservaKafkaPublisher(kafkaTemplate, TOPICO, true).publicar(evento);

        verify(kafkaTemplate).send(TOPICO, "7", evento);
    }

    @Test
    void sinHabitacionUsaLaReservaComoClave() {
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(CompletableFuture.completedFuture(null));
        ReservaStreamEvent evento = evento(null);

        new ReservaKafkaPublisher(kafkaTemplate, TOPICO, true).publicar(evento);

        verify(kafkaTemplate).send(TOPICO, "10", evento);
    }

    @Test
    void siKafkaFallaNoPropagaLaExcepcion() {
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenThrow(new IllegalStateException("broker caido"));

        new ReservaKafkaPublisher(kafkaTemplate, TOPICO, true).publicar(evento(7L));
        // si llegamos aqui sin excepcion, la reserva no se ve afectada por Kafka
    }

    @Test
    void deshabilitadoNoPublica() {
        new ReservaKafkaPublisher(kafkaTemplate, TOPICO, false).publicar(evento(7L));

        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }
}
