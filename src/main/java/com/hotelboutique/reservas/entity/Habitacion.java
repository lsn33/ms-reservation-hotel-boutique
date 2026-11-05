package com.hotelboutique.reservas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "habitaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Habitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tipo tipo;

    @Column(nullable = false)
    private Integer capacidad;

    @Column(name = "precio_por_noche", nullable = false)
    private BigDecimal precioPorNoche;

    @Column(nullable = false)
    @Builder.Default
    private Boolean disponible = true;

    private String descripcion;

    @Column(name = "imagen_url")
    private String imagenUrl;

    public enum Tipo {
        INDIVIDUAL,
        DOBLE,
        SUITE,
        FAMILIAR
    }
}
