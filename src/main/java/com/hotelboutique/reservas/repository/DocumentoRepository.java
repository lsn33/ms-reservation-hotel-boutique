package com.hotelboutique.reservas.repository;

import com.hotelboutique.reservas.entity.Documento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    boolean existsByReservaId(Long reservaId);
}
