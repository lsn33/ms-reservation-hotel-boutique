package com.hotelboutique.reservas.service;

import com.hotelboutique.reservas.entity.OcupacionHabitacion;
import com.hotelboutique.reservas.kafka.ReservaStreamEvent;
import com.hotelboutique.reservas.repository.OcupacionHabitacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Logica de negocio de la ocupacion; no sabe nada de Kafka (la mensajeria vive en OcupacionConsumer). */
@Slf4j
@Service
@RequiredArgsConstructor
public class OcupacionService {

    private final OcupacionHabitacionRepository repository;

    @Transactional
    public void aplicar(ReservaStreamEvent evento) {
        if (evento.habitacionId() == null) {
            log.warn("Evento {} de la reserva {} sin habitacion: no se actualiza la ocupacion",
                    evento.tipo(), evento.reservaId());
            return;
        }

        OcupacionHabitacion ocupacion = repository.findById(evento.habitacionId())
                .orElseGet(() -> OcupacionHabitacion.builder()
                        .habitacionId(evento.habitacionId())
                        .estado(OcupacionHabitacion.Estado.LIBRE)
                        .build());

        switch (evento.tipo()) {
            case "reserva.checkin" -> ocupacion.setEstado(OcupacionHabitacion.Estado.OCUPADA);
            case "reserva.checkout", "reserva.cancelada" -> ocupacion.setEstado(OcupacionHabitacion.Estado.LIBRE);
            default -> {
                // reserva.creada: queda registrada pero no cambia el estado fisico de la habitacion
            }
        }

        ocupacion.setUltimoEvento(evento.tipo());
        ocupacion.setUltimaReservaId(evento.reservaId());
        ocupacion.setActualizadoEn(LocalDateTime.now());
        repository.save(ocupacion);
        log.info("Ocupacion habitacion={} -> {} (evento {})",
                ocupacion.getHabitacionId(), ocupacion.getEstado(), evento.tipo());
    }

    public List<OcupacionHabitacion> listar() {
        return repository.findAll();
    }
}
