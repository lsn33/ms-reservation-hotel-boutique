package com.hotelboutique.reservas.service;

import com.hotelboutique.reservas.dto.ReservaRequest;
import com.hotelboutique.reservas.dto.ReservaResponse;
import com.hotelboutique.reservas.entity.Habitacion;
import com.hotelboutique.reservas.entity.Reserva;
import com.hotelboutique.reservas.repository.HabitacionRepository;
import com.hotelboutique.reservas.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final HabitacionRepository habitacionRepository;

    public ReservaResponse crear(String usuarioEmail, ReservaRequest request) {
        if (!request.getFechaCheckout().isAfter(request.getFechaCheckin())) {
            throw new IllegalArgumentException("La fecha de check-out debe ser posterior al check-in");
        }

        Habitacion habitacion = habitacionRepository.findById(request.getHabitacionId())
                .orElseThrow(() -> new IllegalArgumentException("Habitacion no encontrada"));

        List<Reserva> solapamientos = reservaRepository.buscarSolapamientos(
                habitacion.getId(), request.getFechaCheckin(), request.getFechaCheckout(), Reserva.Estado.CANCELADA
        );

        if (!solapamientos.isEmpty()) {
            throw new IllegalArgumentException("La habitacion ya esta reservada en esas fechas");
        }

        Reserva reserva = Reserva.builder()
                .usuarioEmail(usuarioEmail)
                .habitacion(habitacion)
                .fechaCheckin(request.getFechaCheckin())
                .fechaCheckout(request.getFechaCheckout())
                .estado(Reserva.Estado.CONFIRMADA)
                .build();

        reservaRepository.save(reserva);
        return ReservaResponse.desde(reserva);
    }

    public List<ReservaResponse> listarPorUsuario(String usuarioEmail) {
        return reservaRepository.findByUsuarioEmail(usuarioEmail)
                .stream()
                .map(ReservaResponse::desde)
                .toList();
    }

    public List<ReservaResponse> listarTodas() {
        return reservaRepository.findAll()
                .stream()
                .map(ReservaResponse::desde)
                .toList();
    }

    public ReservaResponse hacerCheckin(Long reservaId) {
        return cambiarEstado(reservaId, Reserva.Estado.CHECK_IN);
    }

    public ReservaResponse hacerCheckout(Long reservaId) {
        return cambiarEstado(reservaId, Reserva.Estado.CHECK_OUT);
    }

    public ReservaResponse cancelar(Long reservaId) {
        return cambiarEstado(reservaId, Reserva.Estado.CANCELADA);
    }

    private ReservaResponse cambiarEstado(Long reservaId, Reserva.Estado nuevoEstado) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        reserva.setEstado(nuevoEstado);
        reservaRepository.save(reserva);
        return ReservaResponse.desde(reserva);
    }
}
