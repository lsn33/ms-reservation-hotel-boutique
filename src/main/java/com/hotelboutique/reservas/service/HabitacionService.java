package com.hotelboutique.reservas.service;

import com.hotelboutique.reservas.dto.HabitacionRequest;
import com.hotelboutique.reservas.entity.Habitacion;
import com.hotelboutique.reservas.repository.HabitacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HabitacionService {

    private final HabitacionRepository habitacionRepository;

    public HabitacionService(HabitacionRepository habitacionRepository) {
        this.habitacionRepository = habitacionRepository;
    }

    public Habitacion crear(HabitacionRequest request) {
        Habitacion habitacion = new Habitacion();
        habitacion.setNombre(request.getNombre());
        habitacion.setTipo(request.getTipo());
        habitacion.setCapacidad(request.getCapacidad());
        habitacion.setPrecioPorNoche(request.getPrecioPorNoche());
        habitacion.setDescripcion(request.getDescripcion());
        habitacion.setImagenUrl(request.getImagenUrl());
        habitacion.setDisponible(true);

        return habitacionRepository.save(habitacion);
    }

    public List<Habitacion> listarTodas() {
        return habitacionRepository.findAll();
    }

    public List<Habitacion> listarDisponibles() {
        return habitacionRepository.findByDisponibleTrue();
    }
}
