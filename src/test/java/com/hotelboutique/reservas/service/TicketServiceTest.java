package com.hotelboutique.reservas.service;

import com.hotelboutique.reservas.entity.Ticket;
import com.hotelboutique.reservas.event.ReservaEvento;
import com.hotelboutique.reservas.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock private TicketRepository ticketRepository;
    @InjectMocks private TicketService ticketService;

    private final ReservaEvento evento = new ReservaEvento(
            "reserva.creada", 1L, "prueba@hotel.cl",
            LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3));

    @Test
    void emitirTicketGuardaTicketConCodigo() {
        when(ticketRepository.existsByReservaId(1L)).thenReturn(false);

        ticketService.emitirTicket(evento);

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(captor.capture());
        Ticket ticket = captor.getValue();
        assertThat(ticket.getReservaId()).isEqualTo(1L);
        assertThat(ticket.getUsuarioEmail()).isEqualTo("prueba@hotel.cl");
        assertThat(ticket.getCodigo()).isNotBlank();
    }

    @Test
    void emitirTicketIgnoraDuplicado() {
        when(ticketRepository.existsByReservaId(1L)).thenReturn(true);

        ticketService.emitirTicket(evento);

        verify(ticketRepository, never()).save(any(Ticket.class));
    }
}
