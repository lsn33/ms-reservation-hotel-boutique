package com.hotelboutique.reservas.service;

import com.hotelboutique.reservas.entity.OcupacionHabitacion;
import com.hotelboutique.reservas.kafka.ReservaStreamEvent;
import com.hotelboutique.reservas.repository.OcupacionHabitacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OcupacionServiceTest {

    @Mock private OcupacionHabitacionRepository repository;
    @InjectMocks private OcupacionService service;

    private static ReservaStreamEvent evento(String tipo, Long habitacionId) {
        return new ReservaStreamEvent("e-1", tipo, 10L, "ana@mail.com", habitacionId, null, null, null, Instant.now());
    }

    private OcupacionHabitacion guardada() {
        ArgumentCaptor<OcupacionHabitacion> captor = ArgumentCaptor.forClass(OcupacionHabitacion.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void checkinMarcaLaHabitacionComoOcupada() {
        when(repository.findById(7L)).thenReturn(Optional.empty());

        service.aplicar(evento("reserva.checkin", 7L));

        OcupacionHabitacion guardada = guardada();
        assertEquals(OcupacionHabitacion.Estado.OCUPADA, guardada.getEstado());
        assertEquals("reserva.checkin", guardada.getUltimoEvento());
        assertEquals(10L, guardada.getUltimaReservaId());
    }

    @Test
    void checkoutLiberaLaHabitacion() {
        when(repository.findById(7L)).thenReturn(Optional.of(OcupacionHabitacion.builder()
                .habitacionId(7L).estado(OcupacionHabitacion.Estado.OCUPADA).build()));

        service.aplicar(evento("reserva.checkout", 7L));

        assertEquals(OcupacionHabitacion.Estado.LIBRE, guardada().getEstado());
    }

    @Test
    void cancelarLiberaLaHabitacion() {
        when(repository.findById(7L)).thenReturn(Optional.of(OcupacionHabitacion.builder()
                .habitacionId(7L).estado(OcupacionHabitacion.Estado.OCUPADA).build()));

        service.aplicar(evento("reserva.cancelada", 7L));

        assertEquals(OcupacionHabitacion.Estado.LIBRE, guardada().getEstado());
    }

    @Test
    void reservaCreadaNoCambiaElEstado() {
        when(repository.findById(7L)).thenReturn(Optional.of(OcupacionHabitacion.builder()
                .habitacionId(7L).estado(OcupacionHabitacion.Estado.OCUPADA).build()));

        service.aplicar(evento("reserva.creada", 7L));

        assertEquals(OcupacionHabitacion.Estado.OCUPADA, guardada().getEstado());
    }

    @Test
    void sinHabitacionNoHaceNada() {
        service.aplicar(evento("reserva.checkin", null));

        verify(repository, never()).save(any());
    }
}
