package com.hotelboutique.reservas.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

/**
 * Manejo de errores de los @KafkaListener (Spring Boot lo aplica solo a todos los listeners):
 * - errores de procesamiento: 3 reintentos con espera creciente (1s, 2s, 4s)
 * - si se agotan, el evento se registra en el log con su particion y offset y se sigue con el siguiente,
 *   asi un mensaje defectuoso no bloquea la particion
 * - mensajes que no se pueden deserializar (JSON roto) no se reintentan: ErrorHandlingDeserializer los marca y se descartan
 */
@Slf4j
@Configuration
public class KafkaErrorHandlingConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(1000L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(5000L);

        return new DefaultErrorHandler(KafkaErrorHandlingConfig::registrarDescartado, backOff);
    }

    private static void registrarDescartado(ConsumerRecord<?, ?> registro, Exception error) {
        log.error("Evento Kafka descartado tras reintentos: topico={} particion={} offset={} clave={} causa={}",
                registro.topic(), registro.partition(), registro.offset(), registro.key(), error.getMessage());
    }
}
