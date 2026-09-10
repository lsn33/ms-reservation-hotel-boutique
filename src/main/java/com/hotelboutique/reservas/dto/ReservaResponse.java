package com.hotelboutique.reservas.dto;

import com.hotelboutique.reservas.entity.Reserva;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ReservaResponse {
    private Long id;
    private String habitacionNombre;
    private String usuarioEmail;
    private LocalDate fechaCheckin;
    private LocalDate fechaCheckout;
    private Reserva.Estado estado;

    public static ReservaResponse desde(Reserva reserva) {
        return new ReservaResponse(
                reserva.getId(),
                reserva.getHabitacion().getNombre(),
                reserva.getUsuarioEmail(),
                reserva.getFechaCheckin(),
                reserva.getFechaCheckout(),
                reserva.getEstado()
        );
    }
}
