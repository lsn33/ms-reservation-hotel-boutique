package com.hotelboutique.reservas.messaging;

import com.hotelboutique.reservas.event.ReservaEvento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaEventPublisherTest {

    @Mock private RabbitTemplate rabbitTemplate;
    @InjectMocks private ReservaEventPublisher publisher;

    private final ReservaEvento evento = new ReservaEvento(
            "reserva.creada", 1L, "prueba@hotel.cl",
            LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3));

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(publisher, "exchangeEventos", "hotel.eventos");
        ReflectionTestUtils.setField(publisher, "exchangeDocumentos", "hotel.documentos");
    }

    @Test
    void publicarEnviaAlExchangeEventosConLaRoutingKeyDelTipo() {
        publisher.publicar(evento);

        verify(rabbitTemplate).convertAndSend("hotel.eventos", "reserva.creada", evento);
    }

    @Test
    void publicarNoLanzaExcepcionSiRabbitFalla() {
        doThrow(new AmqpException("sin conexion"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(ReservaEvento.class));

        assertThatCode(() -> publisher.publicar(evento)).doesNotThrowAnyException();
    }

    @Test
    void publicarComprobanteEnviaAlExchangeDocumentos() {
        publisher.publicarComprobante(evento);

        verify(rabbitTemplate).convertAndSend("hotel.documentos", "comprobante", evento);
    }
}
