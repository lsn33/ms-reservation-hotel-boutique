package com.hotelboutique.reservas.kafka;

import com.hotelboutique.reservas.service.UsuarioReferenciaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Consumer group "reservas-usuarios-cache": mantiene una copia local de los usuarios publicados por ms-auth. */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "hotel.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class UsuarioReferenciaConsumer {

    private final UsuarioReferenciaService usuarioService;

    @KafkaListener(
            topics = "${hotel.kafka.topics.usuarios}",
            groupId = "${hotel.kafka.groups.usuarios}",
            properties = "spring.json.value.default.type=com.hotelboutique.reservas.kafka.UsuarioStreamEvent")
    public void onUsuarioEvento(ConsumerRecord<String, UsuarioStreamEvent> registro) {
        UsuarioStreamEvent evento = registro.value();

        if (evento == null) {
            if (registro.key() != null) {
                usuarioService.eliminar(registro.key());
            }
            return;
        }
        if (!evento.esValido()) {
            log.warn("Evento de usuario invalido descartado (particion={}, offset={}): {}",
                    registro.partition(), registro.offset(), evento);
            return;
        }

        usuarioService.guardar(evento.email(), evento.nombre());
    }
}
