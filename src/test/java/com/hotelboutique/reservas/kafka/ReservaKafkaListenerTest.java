package com.hotelboutique.reservas.kafka;

import com.hotelboutique.reservas.event.ReservaEvento;
import com.hotelboutique.reservas.repository.ReservaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaKafkaListenerTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private ReservaKafkaPublisher publisher;
    @InjectMocks private ReservaKafkaListener listener;

    private static ReservaEvento evento(String tipo) {
        return new ReservaEvento(tipo, 10L, "ana@mail.com", LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3));
    }

    @Test
    void publicaElEventoEnriquecidoConLaHabitacionYElEstado() {
        when(reservaRepository.buscarHabitacionId(10L)).thenReturn(Optional.of(7L));

        listener.onReservaEvento(evento("reserva.checkin"));

        ArgumentCaptor<ReservaStreamEvent> captor = ArgumentCaptor.forClass(ReservaStreamEvent.class);
        verify(publisher).publicar(captor.capture());
        ReservaStreamEvent publicado = captor.getValue();
        assertEquals("reserva.checkin", publicado.tipo());
        assertEquals(7L, publicado.habitacionId());
        assertEquals("CHECK_IN", publicado.estado());
        assertEquals("ana@mail.com", publicado.usuarioEmail());
        assertNotNull(publicado.eventId());
        assertNotNull(publicado.ocurridoEn());
    }

    @Test
    void siNoEncuentraLaHabitacionIgualPublica() {
        when(reservaRepository.buscarHabitacionId(10L)).thenReturn(Optional.empty());

        listener.onReservaEvento(evento("reserva.creada"));

        ArgumentCaptor<ReservaStreamEvent> captor = ArgumentCaptor.forClass(ReservaStreamEvent.class);
        verify(publisher).publicar(captor.capture());
        assertEquals(null, captor.getValue().habitacionId());
    }

    @Test
    void unErrorNoSePropagaParaNoAfectarARabbitMq() {
        when(reservaRepository.buscarHabitacionId(10L)).thenThrow(new IllegalStateException("db caida"));

        listener.onReservaEvento(evento("reserva.cancelada"));

        verify(publisher, org.mockito.Mockito.never()).publicar(any());
    }
}
