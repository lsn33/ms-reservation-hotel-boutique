package com.hotelboutique.reservas.service;

import com.hotelboutique.reservas.entity.Documento;
import com.hotelboutique.reservas.event.ReservaEvento;
import com.hotelboutique.reservas.repository.DocumentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentoServiceTest {

    @Mock private DocumentoRepository documentoRepository;
    @InjectMocks private DocumentoService documentoService;

    private final ReservaEvento evento = new ReservaEvento(
            "reserva.checkout", 1L, "prueba@hotel.cl",
            LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3));

    @Test
    void generarComprobanteGuardaDocumentoConContenido() {
        when(documentoRepository.existsByReservaId(1L)).thenReturn(false);

        documentoService.generarComprobante(evento);

        ArgumentCaptor<Documento> captor = ArgumentCaptor.forClass(Documento.class);
        verify(documentoRepository).save(captor.capture());
        Documento documento = captor.getValue();
        assertThat(documento.getReservaId()).isEqualTo(1L);
        assertThat(documento.getUsuarioEmail()).isEqualTo("prueba@hotel.cl");
        assertThat(documento.getCodigo()).isNotBlank();
        assertThat(documento.getContenido()).contains("reserva 1", "prueba@hotel.cl");
    }

    @Test
    void generarComprobanteIgnoraDuplicado() {
        when(documentoRepository.existsByReservaId(1L)).thenReturn(true);

        documentoService.generarComprobante(evento);

        verify(documentoRepository, never()).save(any(Documento.class));
    }
}
