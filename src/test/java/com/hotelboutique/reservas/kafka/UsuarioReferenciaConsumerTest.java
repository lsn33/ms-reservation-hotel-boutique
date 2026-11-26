package com.hotelboutique.reservas.kafka;

import com.hotelboutique.reservas.service.UsuarioReferenciaService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UsuarioReferenciaConsumerTest {

    @Mock private UsuarioReferenciaService usuarioService;
    @InjectMocks private UsuarioReferenciaConsumer consumer;

    @Test
    void guardaElUsuarioDelEvento() {
        UsuarioStreamEvent evento = new UsuarioStreamEvent("e-1", "usuario.sincronizado", "ana@mail.com", "Ana", Instant.now());

        consumer.onUsuarioEvento(new ConsumerRecord<>("hotel.usuarios.eventos", 0, 5L, "ana@mail.com", evento));

        verify(usuarioService).guardar("ana@mail.com", "Ana");
    }

    @Test
    void tombstoneEliminaAlUsuario() {
        consumer.onUsuarioEvento(new ConsumerRecord<>("hotel.usuarios.eventos", 0, 6L, "ana@mail.com", null));

        verify(usuarioService).eliminar("ana@mail.com");
    }

    @Test
    void eventoSinEmailSeDescarta() {
        UsuarioStreamEvent invalido = new UsuarioStreamEvent("e-2", "usuario.sincronizado", " ", "Ana", Instant.now());

        consumer.onUsuarioEvento(new ConsumerRecord<>("hotel.usuarios.eventos", 0, 7L, "x", invalido));

        verify(usuarioService, never()).guardar(anyString(), anyString());
    }
}
