package com.hotelboutique.reservas.service;

import com.hotelboutique.reservas.dto.ReservaRequest;
import com.hotelboutique.reservas.dto.ReservaResponse;
import com.hotelboutique.reservas.entity.Habitacion;
import com.hotelboutique.reservas.entity.Reserva;
import com.hotelboutique.reservas.event.ReservaEvento;
import com.hotelboutique.reservas.repository.HabitacionRepository;
import com.hotelboutique.reservas.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final HabitacionRepository habitacionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
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
        publicarEvento("reserva.creada", reserva);
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

    @Transactional
    public ReservaResponse hacerCheckin(Long reservaId) {
        return cambiarEstado(reservaId, Reserva.Estado.CHECK_IN, "reserva.checkin");
    }

    @Transactional
    public ReservaResponse hacerCheckout(Long reservaId) {
        return cambiarEstado(reservaId, Reserva.Estado.CHECK_OUT, "reserva.checkout");
    }

    @Transactional
    public ReservaResponse cancelar(Long reservaId) {
        return cambiarEstado(reservaId, Reserva.Estado.CANCELADA, "reserva.cancelada");
    }

    private ReservaResponse cambiarEstado(Long reservaId, Reserva.Estado nuevoEstado, String tipoEvento) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        reserva.setEstado(nuevoEstado);
        reservaRepository.save(reserva);
        publicarEvento(tipoEvento, reserva);
        return ReservaResponse.desde(reserva);
    }

    private void publicarEvento(String tipo, Reserva reserva) {
        eventPublisher.publishEvent(new ReservaEvento(
                tipo,
                reserva.getId(),
                reserva.getUsuarioEmail(),
                reserva.getFechaCheckin(),
                reserva.getFechaCheckout()
        ));
    }
}
