package com.hotelboutique.reservas.kafka;

import com.hotelboutique.reservas.service.OcupacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumer group "reservas-ocupacion": reacciona en tiempo real a los eventos de reserva
 * (check-in => habitacion OCUPADA, check-out o cancelacion => LIBRE).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "hotel.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class OcupacionConsumer {

    private final OcupacionService ocupacionService;

    @KafkaListener(
            topics = "${hotel.kafka.topics.reservas}",
            groupId = "${hotel.kafka.groups.ocupacion}",
            properties = "spring.json.value.default.type=com.hotelboutique.reservas.kafka.ReservaStreamEvent")
    public void onReservaEvento(ConsumerRecord<String, ReservaStreamEvent> registro) {
        ReservaStreamEvent evento = registro.value();

        if (evento == null || !evento.esValido()) {
            // Payload invalido: reintentar no lo arregla, se registra y se avanza al siguiente
            log.warn("Evento de reserva invalido descartado (particion={}, offset={}): {}",
                    registro.partition(), registro.offset(), evento);
            return;
        }

        log.info("Evento recibido {} reserva={} (particion={}, offset={})",
                evento.tipo(), evento.reservaId(), registro.partition(), registro.offset());
        ocupacionService.aplicar(evento);
    }
}
