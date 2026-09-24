package com.hotelboutique.reservas.service;

import com.hotelboutique.reservas.dto.HabitacionRequest;
import com.hotelboutique.reservas.entity.Habitacion;
import com.hotelboutique.reservas.repository.HabitacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HabitacionService {

    private final HabitacionRepository habitacionRepository;

    public Habitacion crear(HabitacionRequest request) {
        Habitacion habitacion = Habitacion.builder()
                .nombre(request.getNombre())
                .tipo(request.getTipo())
                .capacidad(request.getCapacidad())
                .precioPorNoche(request.getPrecioPorNoche())
                .descripcion(request.getDescripcion())
                .imagenUrl(request.getImagenUrl())
                .disponible(true)
                .build();

        return habitacionRepository.save(habitacion);
    }

    public List<Habitacion> listarTodas() {
        return habitacionRepository.findAll();
    }

    public List<Habitacion> listarDisponibles() {
        return habitacionRepository.findByDisponibleTrue();
    }
}
