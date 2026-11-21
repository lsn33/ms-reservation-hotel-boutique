package com.hotelboutique.reservas.kafka;

import java.time.Instant;

/**
 * Evento de usuario que produce ms-auth en el topico compactado hotel.usuarios.eventos (clave = email).
 * Esta es la copia del lado consumidor: ms-reservas solo lee los campos que necesita.
 */
public record UsuarioStreamEvent(
        String eventId,
        String tipo,
        String email,
        String nombre,
        Instant ocurridoEn
) {

    public boolean esValido() {
        return email != null && !email.isBlank();
    }
}
