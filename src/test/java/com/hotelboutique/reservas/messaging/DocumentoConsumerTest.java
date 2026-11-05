package com.hotelboutique.reservas.messaging;

import com.hotelboutique.reservas.event.ReservaEvento;
import com.hotelboutique.reservas.service.DocumentoService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentoConsumerTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    @Mock private DocumentoService documentoService;
    @Mock private Channel channel;
    @InjectMocks private DocumentoConsumer consumer;

    private ReservaEvento evento(String usuarioEmail) {
        return new ReservaEvento("reserva.checkout", 1L, usuarioEmail,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3));
    }

    private Message mensaje(ReservaEvento evento) {
        return new Message(MAPPER.writeValueAsBytes(evento));
    }

    @Test
    void procesaYConfirmaConAck() throws Exception {
        ReservaEvento evento = evento("prueba@hotel.cl");

        consumer.onComprobante(mensaje(evento), channel, 1L);

        verify(documentoService).generarComprobante(evento);
        verify(channel).basicAck(1L, false);
    }

    @Test
    void datosInvalidosHacenNackSinRequeue() throws Exception {
        consumer.onComprobante(mensaje(evento(null)), channel, 7L);

        verify(channel).basicNack(7L, false, false);
        verifyNoInteractions(documentoService);
    }

    @Test
    void jsonMalformadoHaceNackSinRequeue() throws Exception {
        Message roto = new Message("{esto no es json".getBytes(StandardCharsets.UTF_8));

        consumer.onComprobante(roto, channel, 9L);

        verify(channel).basicNack(9L, false, false);
        verifyNoInteractions(documentoService);
    }

    @Test
    void errorRecuperableSePropagaSinConfirmar() throws Exception {
        ReservaEvento evento = evento("prueba@hotel.cl");
        doThrow(new RuntimeException("base de datos no disponible"))
                .when(documentoService).generarComprobante(evento);

        assertThatThrownBy(() -> consumer.onComprobante(mensaje(evento), channel, 3L))
                .isInstanceOf(RuntimeException.class);

        verify(channel, never()).basicAck(anyLong(), anyBoolean());
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }
}