package com.hotelboutique.reservas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String codigo;

    @Column(name = "reserva_id", nullable = false, unique = true)
    private Long reservaId;

    @Column(name = "usuario_email", nullable = false)
    private String usuarioEmail;

    @Column(name = "emitido_en", nullable = false, updatable = false)
    private LocalDateTime emitidoEn;

    @PrePersist
    protected void onCreate() {
        this.emitidoEn = LocalDateTime.now();
    }
}
