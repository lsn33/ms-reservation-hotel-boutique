package com.hotelboutique.reservas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Copia local (solo lectura) de los usuarios que publica ms-auth en Kafka, para no consultar a ms-auth por HTTP. */
@Entity
@Table(name = "usuarios_referencia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioReferencia {

    @Id
    private String email;

    private String nombre;

    @Column(name = "ultima_sincronizacion")
    private LocalDateTime ultimaSincronizacion;
}
