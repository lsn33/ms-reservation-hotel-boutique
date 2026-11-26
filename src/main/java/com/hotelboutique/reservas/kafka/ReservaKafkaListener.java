package com.hotelboutique.reservas.kafka;

import com.hotelboutique.reservas.event.ReservaEvento;
import com.hotelboutique.reservas.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Puente hacia Kafka: escucha el mismo ReservaEvento que ya usa RabbitMQ (ReservaEventListener) y lo publica
 * en Kafka despues del commit. No toca el flujo de RabbitMQ.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservaKafkaListener {

    private final ReservaRepository reservaRepository;
    private final ReservaKafkaPublisher publisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReservaEvento(ReservaEvento evento) {
        try {
            Long habitacionId = evento.reservaId() == null
                    ? null
                    : reservaRepository.buscarHabitacionId(evento.reservaId()).orElse(null);
            publisher.publicar(ReservaStreamEvent.desde(evento, habitacionId));
        } catch (RuntimeException ex) {
            // Cualquier error aqui no debe interrumpir los demas listeners post-commit (RabbitMQ)
            log.error("No se pudo preparar el evento Kafka {} de la reserva {}: {}",
                    evento.tipo(), evento.reservaId(), ex.getMessage(), ex);
        }
    }
}
