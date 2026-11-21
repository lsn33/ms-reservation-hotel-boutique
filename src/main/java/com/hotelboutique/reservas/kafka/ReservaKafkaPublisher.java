package com.hotelboutique.reservas.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** Productor Kafka de eventos de reserva. Un fallo de Kafka se registra pero nunca afecta la reserva ni a RabbitMQ. */
@Component
public class ReservaKafkaPublisher {

    private static final Logger log = LoggerFactory.getLogger(ReservaKafkaPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;
    private final boolean habilitado;

    public ReservaKafkaPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                 @Value("${hotel.kafka.topics.reservas}") String topic,
                                 @Value("${hotel.kafka.enabled:true}") boolean habilitado) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.habilitado = habilitado;
    }

    public void publicar(ReservaStreamEvent evento) {
        if (!habilitado) {
            return;
        }
        // La clave define la particion: todos los eventos de una habitacion quedan en orden dentro de la misma particion
        String clave = String.valueOf(evento.habitacionId() != null ? evento.habitacionId() : evento.reservaId());
        try {
            kafkaTemplate.send(topic, clave, evento).whenComplete((resultado, error) -> {
                if (error != null) {
                    log.error("No se pudo publicar en Kafka {} reserva={}: {}",
                            evento.tipo(), evento.reservaId(), error.getMessage());
                } else if (resultado != null) {
                    log.info("Kafka publicado: {} reserva={} topico={} particion={} offset={}",
                            evento.tipo(), evento.reservaId(), topic,
                            resultado.getRecordMetadata().partition(), resultado.getRecordMetadata().offset());
                }
            });
        } catch (RuntimeException ex) {
            log.error("Kafka no disponible, evento {} de la reserva {} no publicado: {}",
                    evento.tipo(), evento.reservaId(), ex.getMessage());
        }
    }
}
