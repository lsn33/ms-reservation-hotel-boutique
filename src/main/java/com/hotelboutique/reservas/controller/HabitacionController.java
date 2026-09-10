package com.hotelboutique.reservas.controller;

import com.hotelboutique.reservas.dto.HabitacionRequest;
import com.hotelboutique.reservas.entity.Habitacion;
import com.hotelboutique.reservas.service.HabitacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/habitaciones")
@RequiredArgsConstructor
public class HabitacionController {

    private final HabitacionService habitacionService;

    @PostMapping
    public ResponseEntity<Habitacion> crear(@Valid @RequestBody HabitacionRequest request) {
        return ResponseEntity.ok(habitacionService.crear(request));
    }

    @GetMapping
    public ResponseEntity<List<Habitacion>> listarTodas() {
        return ResponseEntity.ok(habitacionService.listarTodas());
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<Habitacion>> listarDisponibles() {
        return ResponseEntity.ok(habitacionService.listarDisponibles());
    }
}
