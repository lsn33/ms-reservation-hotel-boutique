package com.hotelboutique.reservas.repository;

import com.hotelboutique.reservas.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    boolean existsByReservaId(Long reservaId);
}
