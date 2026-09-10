package com.hotelboutique.reservas.dto;

import com.hotelboutique.reservas.entity.Habitacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HabitacionRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotNull(message = "El tipo es obligatorio")
    private Habitacion.Tipo tipo;

    @NotNull(message = "La capacidad es obligatoria")
    @Positive(message = "La capacidad debe ser mayor a 0")
    private Integer capacidad;

    @NotNull(message = "El precio por noche es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    private BigDecimal precioPorNoche;

    private String descripcion;
}
