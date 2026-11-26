package com.hotelboutique.reservas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Vista de ocupacion por habitacion, mantenida en tiempo real a partir de los eventos de Kafka. */
@Entity
@Table(name = "ocupacion_habitaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OcupacionHabitacion {

    @Id
    @Column(name = "habitacion_id")
    private Long habitacionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado;

    @Column(name = "ultimo_evento")
    private String ultimoEvento;

    @Column(name = "ultima_reserva_id")
    private Long ultimaReservaId;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    public enum Estado {
        LIBRE, OCUPADA
    }
}
