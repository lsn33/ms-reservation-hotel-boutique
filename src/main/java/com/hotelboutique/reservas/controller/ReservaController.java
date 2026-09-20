package com.hotelboutique.reservas.controller;

import com.hotelboutique.reservas.dto.ReservaRequest;
import com.hotelboutique.reservas.dto.ReservaResponse;
import com.hotelboutique.reservas.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    @PostMapping
    public ResponseEntity<ReservaResponse> crear(Authentication auth, @Valid @RequestBody ReservaRequest request) {
        String usuarioEmail = auth.getName(); // viene del JWT, lo puso el JwtFilter (sin consultar BD)
        return ResponseEntity.ok(reservaService.crear(usuarioEmail, request));
    }

    @GetMapping("/mias")
    public ResponseEntity<List<ReservaResponse>> misReservas(Authentication auth) {
        return ResponseEntity.ok(reservaService.listarPorUsuario(auth.getName()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReservaResponse>> listarTodas() {
        return ResponseEntity.ok(reservaService.listarTodas());
    }

    @PutMapping("/{id}/checkin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReservaResponse> checkin(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.hacerCheckin(id));
    }

    @PutMapping("/{id}/checkout")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReservaResponse> checkout(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.hacerCheckout(id));
    }

    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReservaResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.cancelar(id));
    }
}
