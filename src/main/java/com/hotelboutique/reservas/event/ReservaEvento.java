package com.hotelboutique.reservas.event;

import java.time.LocalDate;

public record ReservaEvento(
        String tipo,
        Long reservaId,
        String usuarioEmail,
        LocalDate fechaCheckin,
        LocalDate fechaCheckout
) {}
