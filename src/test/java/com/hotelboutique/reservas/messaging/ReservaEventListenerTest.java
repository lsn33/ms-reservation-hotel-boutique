package com.hotelboutique.reservas.messaging;

import com.hotelboutique.reservas.event.ReservaEvento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaEventListenerTest {

    @Mock private ReservaEventPublisher publisher;
    @InjectMocks private ReservaEventListener listener;

    @Test
    void reservaCreadaSoloPublicaElEvento() {
        ReservaEvento evento = evento("reserva.creada");

        listener.onReservaEvento(evento);

        verify(publisher).publicar(evento);
        verify(publisher, never()).publicarComprobante(any());
    }

    @Test
    void checkoutPublicaEventoYSolicitaComprobante() {
        ReservaEvento evento = evento("reserva.checkout");

        listener.onReservaEvento(evento);

        verify(publisher).publicar(evento);
        verify(publisher).publicarComprobante(evento);
    }

    private ReservaEvento evento(String tipo) {
        return new ReservaEvento(tipo, 1L, "prueba@hotel.cl",
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3));
    }
}
