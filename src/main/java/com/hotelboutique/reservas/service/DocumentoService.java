package com.hotelboutique.reservas.service;

import com.hotelboutique.reservas.entity.Documento;
import com.hotelboutique.reservas.event.ReservaEvento;
import com.hotelboutique.reservas.repository.DocumentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentoService {

    private final DocumentoRepository documentoRepository;

    @Transactional
    public void generarComprobante(ReservaEvento evento) {
        if (documentoRepository.existsByReservaId(evento.reservaId())) {
            log.warn("Comprobante ya generado para la reserva {}, se ignora el duplicado", evento.reservaId());
            return;
        }

        String contenido = String.format(
                "Comprobante de reserva %d para %s. Check-in: %s. Check-out: %s.",
                evento.reservaId(), evento.usuarioEmail(),
                evento.fechaCheckin(), evento.fechaCheckout());

        Documento documento = Documento.builder()
                .codigo(UUID.randomUUID().toString())
                .reservaId(evento.reservaId())
                .usuarioEmail(evento.usuarioEmail())
                .contenido(contenido)
                .build();

        documentoRepository.save(documento);
        log.info("Comprobante {} generado para la reserva {}", documento.getCodigo(), evento.reservaId());
    }
}
