package com.hotelboutique.reservas.dto;

import com.hotelboutique.reservas.entity.Reserva;
import java.time.LocalDate;

public class ReservaResponse {
    private Long id;
    private String habitacionNombre;
    private String usuarioEmail;
    private LocalDate fechaCheckin;
    private LocalDate fechaCheckout;
    private Reserva.Estado estado;

    public ReservaResponse(Long id, String habitacionNombre, String usuarioEmail,
                           LocalDate fechaCheckin, LocalDate fechaCheckout, Reserva.Estado estado) {
        this.id = id;
        this.habitacionNombre = habitacionNombre;
        this.usuarioEmail = usuarioEmail;
        this.fechaCheckin = fechaCheckin;
        this.fechaCheckout = fechaCheckout;
        this.estado = estado;
    }

    public static ReservaResponse desde(Reserva reserva) {
        return new ReservaResponse(
                reserva.getId(),
                reserva.getHabitacion().getNombre(),
                reserva.getUsuarioEmail(),
                reserva.getFechaCheckin(),
                reserva.getFechaCheckout(),
                reserva.getEstado());
    }

    public Long getId() { return id; }
    public String getHabitacionNombre() { return habitacionNombre; }
    public String getUsuarioEmail() { return usuarioEmail; }
    public LocalDate getFechaCheckin() { return fechaCheckin; }
    public LocalDate getFechaCheckout() { return fechaCheckout; }
    public Reserva.Estado getEstado() { return estado; }
}