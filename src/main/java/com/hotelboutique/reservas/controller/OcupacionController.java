package com.hotelboutique.reservas.controller;

import com.hotelboutique.reservas.entity.OcupacionHabitacion;
import com.hotelboutique.reservas.service.OcupacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/habitaciones/ocupacion")
@RequiredArgsConstructor
public class OcupacionController {

    private final OcupacionService ocupacionService;

    /** Ocupacion actual de las habitaciones, alimentada por los eventos de Kafka. */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OcupacionHabitacion>> listar() {
        return ResponseEntity.ok(ocupacionService.listar());
    }
}
