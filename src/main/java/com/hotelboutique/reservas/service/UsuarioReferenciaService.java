package com.hotelboutique.reservas.service;

import com.hotelboutique.reservas.entity.UsuarioReferencia;
import com.hotelboutique.reservas.repository.UsuarioReferenciaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioReferenciaService {

    private final UsuarioReferenciaRepository repository;

    /** Guarda o actualiza el usuario: como el topico esta compactado, siempre llega el ultimo estado de cada email. */
    @Transactional
    public void guardar(String email, String nombre) {
        repository.save(UsuarioReferencia.builder()
                .email(email)
                .nombre(nombre)
                .ultimaSincronizacion(LocalDateTime.now())
                .build());
        log.info("Usuario de referencia actualizado: {}", email);
    }

    /** Un mensaje con valor nulo (tombstone) en un topico compactado significa "borrar esta clave". */
    @Transactional
    public void eliminar(String email) {
        if (repository.existsById(email)) {
            repository.deleteById(email);
            log.info("Usuario de referencia eliminado por tombstone: {}", email);
        }
    }
}
