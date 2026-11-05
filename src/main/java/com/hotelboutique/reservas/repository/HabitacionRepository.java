package com.hotelboutique.reservas.repository;

import com.hotelboutique.reservas.entity.Habitacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {
    List<Habitacion> findByDisponibleTrue();
}
