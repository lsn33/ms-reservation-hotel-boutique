package com.hotelboutique.reservas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ya no es una relacion @ManyToOne a un Usuario: cada microservicio es dueno de sus propios datos.
    // El email viene del JWT (lo valida JwtFilter y lo pone en el Authentication).
    @Column(name = "usuario_email", nullable = false)
    private String usuarioEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habitacion_id", nullable = false)
    private Habitacion habitacion;

    @Column(name = "fecha_checkin", nullable = false)
    private LocalDate fechaCheckin;

    @Column(name = "fecha_checkout", nullable = false)
    private LocalDate fechaCheckout;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Estado estado = Estado.CONFIRMADA;

    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    protected void onCreate() {
        this.creadoEn = LocalDateTime.now();
    }

    public enum Estado {
        CONFIRMADA,
        CHECK_IN,
        CHECK_OUT,
        CANCELADA
    }
}
