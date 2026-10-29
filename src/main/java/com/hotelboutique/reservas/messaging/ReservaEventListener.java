package com.hotelboutique.reservas.messaging;

import com.hotelboutique.reservas.event.ReservaEvento;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ReservaEventListener {

    private static final String TIPO_CHECKOUT = "reserva.checkout";

    private final ReservaEventPublisher publisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReservaEvento(ReservaEvento evento) {
        publisher.publicar(evento);
        if (TIPO_CHECKOUT.equals(evento.tipo())) {
            publisher.publicarComprobante(evento);
        }
    }
}
