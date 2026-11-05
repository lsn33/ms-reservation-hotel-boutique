package com.hotelboutique.reservas.service;

import com.hotelboutique.reservas.entity.Ticket;
import com.hotelboutique.reservas.event.ReservaEvento;
import com.hotelboutique.reservas.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    @Transactional
    public void emitirTicket(ReservaEvento evento) {
        if (ticketRepository.existsByReservaId(evento.reservaId())) {
            log.warn("Ticket ya emitido para la reserva {}, se ignora el duplicado", evento.reservaId());
            return;
        }

        Ticket ticket = Ticket.builder()
                .codigo(UUID.randomUUID().toString())
                .reservaId(evento.reservaId())
                .usuarioEmail(evento.usuarioEmail())
                .build();

        ticketRepository.save(ticket);
        log.info("Ticket {} emitido para la reserva {}", ticket.getCodigo(), evento.reservaId());
    }
}
