package com.hotelboutique.reservas.repository;

import com.hotelboutique.reservas.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByUsuarioEmail(String usuarioEmail);

    @Query("""
            SELECT r FROM Reserva r
            WHERE r.habitacion.id = :habitacionId
            AND r.estado <> :estadoCancelada
            AND r.fechaCheckin < :fechaCheckout
            AND r.fechaCheckout > :fechaCheckin
            """)
    List<Reserva> buscarSolapamientos(
            @Param("habitacionId") Long habitacionId,
            @Param("fechaCheckin") LocalDate fechaCheckin,
            @Param("fechaCheckout") LocalDate fechaCheckout,
            @Param("estadoCancelada") Reserva.Estado estadoCancelada
    );
}
