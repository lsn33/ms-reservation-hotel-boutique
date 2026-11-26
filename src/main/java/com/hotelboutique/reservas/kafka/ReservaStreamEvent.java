package com.hotelboutique.reservas.kafka;

import com.hotelboutique.reservas.event.ReservaEvento;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Evento de reserva que viaja por el topico hotel.reservas.eventos.
 * Cada campo responde a un caso de uso:
 * - eventId: identifica el evento (trazabilidad y deteccion de duplicados)
 * - tipo: reserva.creada / checkin / checkout / cancelada (que ocurrio)
 * - reservaId, usuarioEmail: de quien y cual reserva (actividad del usuario)
 * - habitacionId: sobre que habitacion actuar (ocupacion)
 * - fechaCheckin / fechaCheckout / estado: foto de la reserva sin consultar a ms-reservas
 * - ocurridoEn: cuando paso, para ordenar y medir retrasos
 */
public record ReservaStreamEvent(
        String eventId,
        String tipo,
        Long reservaId,
        String usuarioEmail,
        Long habitacionId,
        LocalDate fechaCheckin,
        LocalDate fechaCheckout,
        String estado,
        Instant ocurridoEn
) {

    public static ReservaStreamEvent desde(ReservaEvento evento, Long habitacionId) {
        return new ReservaStreamEvent(
                UUID.randomUUID().toString(),
                evento.tipo(),
                evento.reservaId(),
                evento.usuarioEmail(),
                habitacionId,
                evento.fechaCheckin(),
                evento.fechaCheckout(),
                estadoDe(evento.tipo()),
                Instant.now());
    }

    private static String estadoDe(String tipo) {
        if (tipo == null) {
            return null;
        }
        return switch (tipo) {
            case "reserva.creada" -> "CONFIRMADA";
            case "reserva.checkin" -> "CHECK_IN";
            case "reserva.checkout" -> "CHECK_OUT";
            case "reserva.cancelada" -> "CANCELADA";
            default -> null;
        };
    }

    /** Un evento sin tipo o sin reserva no sirve para ningun consumidor. */
    public boolean esValido() {
        return tipo != null && !tipo.isBlank() && reservaId != null;
    }
}
