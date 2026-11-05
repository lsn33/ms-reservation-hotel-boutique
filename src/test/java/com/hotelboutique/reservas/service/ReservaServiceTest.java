package com.hotelboutique.reservas.service;

import com.hotelboutique.reservas.dto.ReservaRequest;
import com.hotelboutique.reservas.entity.Habitacion;
import com.hotelboutique.reservas.entity.Reserva;
import com.hotelboutique.reservas.event.ReservaEvento;
import com.hotelboutique.reservas.repository.HabitacionRepository;
import com.hotelboutique.reservas.repository.ReservaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private HabitacionRepository habitacionRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @InjectMocks private ReservaService reservaService;

    private final LocalDate checkin = LocalDate.of(2026, 11, 1);
    private final LocalDate checkout = LocalDate.of(2026, 11, 3);

    private Habitacion habitacion() {
        return Habitacion.builder()
                .id(1L)
                .nombre("Suite Prueba")
                .tipo(Habitacion.Tipo.SUITE)
                .capacidad(2)
                .precioPorNoche(new BigDecimal("100000"))
                .build();
    }

    private ReservaRequest request(LocalDate in, LocalDate out) {
        ReservaRequest req = mock(ReservaRequest.class);
        when(req.getFechaCheckin()).thenReturn(in);
        when(req.getFechaCheckout()).thenReturn(out);
        return req;
    }

    @Test
    void crearPublicaEventoReservaCreada() {
        ReservaRequest req = request(checkin, checkout);
        when(req.getHabitacionId()).thenReturn(1L);
        when(habitacionRepository.findById(1L)).thenReturn(Optional.of(habitacion()));
        when(reservaRepository.buscarSolapamientos(anyLong(), any(), any(), any())).thenReturn(List.of());

        reservaService.crear("prueba@hotel.cl", req);

        verify(reservaRepository).save(any(Reserva.class));
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(captor.capture());
        ReservaEvento evento = (ReservaEvento) captor.getValue();
        assertThat(evento.tipo()).isEqualTo("reserva.creada");
        assertThat(evento.usuarioEmail()).isEqualTo("prueba@hotel.cl");
        assertThat(evento.fechaCheckin()).isEqualTo(checkin);
        assertThat(evento.fechaCheckout()).isEqualTo(checkout);
    }

    @Test
    void crearRechazaCheckoutAnteriorAlCheckin() {
        ReservaRequest req = request(checkout, checkin);

        assertThatThrownBy(() -> reservaService.crear("prueba@hotel.cl", req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("check-out");

        verifyNoInteractions(reservaRepository, eventPublisher);
    }

    @Test
    void crearRechazaHabitacionOcupadaSinPublicarEvento() {
        ReservaRequest req = request(checkin, checkout);
        when(req.getHabitacionId()).thenReturn(1L);
        when(habitacionRepository.findById(1L)).thenReturn(Optional.of(habitacion()));
        when(reservaRepository.buscarSolapamientos(anyLong(), any(), any(), any()))
                .thenReturn(List.of(Reserva.builder().build()));

        assertThatThrownBy(() -> reservaService.crear("prueba@hotel.cl", req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya esta reservada");

        verify(reservaRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void hacerCheckinPublicaReservaCheckin() {
        Reserva existente = reservaExistente();
        when(reservaRepository.findById(5L)).thenReturn(Optional.of(existente));

        reservaService.hacerCheckin(5L);

        assertThat(existente.getEstado()).isEqualTo(Reserva.Estado.CHECK_IN);
        assertTipoPublicado("reserva.checkin");
    }

    @Test
    void hacerCheckoutPublicaReservaCheckout() {
        Reserva existente = reservaExistente();
        when(reservaRepository.findById(5L)).thenReturn(Optional.of(existente));

        reservaService.hacerCheckout(5L);

        assertThat(existente.getEstado()).isEqualTo(Reserva.Estado.CHECK_OUT);
        assertTipoPublicado("reserva.checkout");
    }

    @Test
    void cancelarPublicaReservaCancelada() {
        Reserva existente = reservaExistente();
        when(reservaRepository.findById(5L)).thenReturn(Optional.of(existente));

        reservaService.cancelar(5L);

        assertThat(existente.getEstado()).isEqualTo(Reserva.Estado.CANCELADA);
        assertTipoPublicado("reserva.cancelada");
    }

    private Reserva reservaExistente() {
        return Reserva.builder()
                .id(5L)
                .usuarioEmail("prueba@hotel.cl")
                .habitacion(habitacion())
                .fechaCheckin(checkin)
                .fechaCheckout(checkout)
                .estado(Reserva.Estado.CONFIRMADA)
                .build();
    }

    private void assertTipoPublicado(String tipoEsperado) {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(((ReservaEvento) captor.getValue()).tipo()).isEqualTo(tipoEsperado);
    }
}
