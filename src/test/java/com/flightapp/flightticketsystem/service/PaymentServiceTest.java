package com.flightapp.flightticketsystem.service;

import com.flightapp.flightticketsystem.dto.PaymentRequestDto;
import com.flightapp.flightticketsystem.entities.*;
import com.flightapp.flightticketsystem.exception.BaseException;
import com.flightapp.flightticketsystem.repository.*;
import com.flightapp.flightticketsystem.service.impl.PaymentServiceImpl;
import com.iyzipay.Options;
import com.iyzipay.model.Payment;
import com.iyzipay.request.CreatePaymentRequest;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import java.math.BigDecimal;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {
    private final FlightRepository flights = mock(FlightRepository.class);
    private final SeatRepository seats = mock(SeatRepository.class);
    private final TicketRepository tickets = mock(TicketRepository.class);
    private final PaymentServiceImpl service = new PaymentServiceImpl(new Options(), flights, seats,
            tickets, mock(UserRepository.class));

    private Seat prepare() {
        Flight flight = new Flight();
        flight.setId(1);
        Seat seat = new Seat();
        seat.setId(2);
        seat.setFlight(flight);
        seat.setPrice(new BigDecimal("100.00"));
        seat.setAvailable(true);
        when(flights.findById(1)).thenReturn(Optional.of(flight));
        when(seats.findByIdForUpdate(2)).thenReturn(Optional.of(seat));
        return seat;
    }

    private PaymentRequestDto request() {
        PaymentRequestDto request = new PaymentRequestDto();
        request.setFlightId(1);
        request.setSeatId(2);
        request.setAmount(BigDecimal.ONE);
        return request;
    }

    @Test
    void unavailableSeatIsRejectedBeforeContactingPaymentProvider() {
        Seat seat = prepare();
        seat.setAvailable(false);
        try (MockedStatic<Payment> api = mockStatic(Payment.class)) {
            assertThrows(BaseException.class, () -> service.createPayment(request()));
            api.verifyNoInteractions();
            verifyNoInteractions(tickets);
        }
    }

    @Test
    void rejectedPaymentDoesNotCreateTicketOrOccupySeat() {
        Seat seat = prepare();
        Payment payment = new Payment();
        payment.setStatus("failure");
        try (MockedStatic<Payment> api = mockStatic(Payment.class)) {
            api.when(() -> Payment.create(any(CreatePaymentRequest.class), any(Options.class))).thenReturn(payment);
            assertEquals("FAILED", service.createPayment(request()).getStatus());
            assertTrue(seat.isAvailable());
            verifyNoInteractions(tickets);
            verify(seats, never()).save(any());
        }
    }

    @Test
    void successUsesDatabasePriceAndCreatesTicket() {
        Seat seat = prepare();
        Payment payment = new Payment();
        payment.setStatus("success");
        payment.setPaymentId("test-payment");
        when(tickets.save(any())).thenAnswer(call -> call.getArgument(0));
        try (MockedStatic<Payment> api = mockStatic(Payment.class)) {
            api.when(() -> Payment.create(any(CreatePaymentRequest.class), any(Options.class)))
                    .thenAnswer(call -> {
                        CreatePaymentRequest sent = call.getArgument(0);
                        assertEquals(seat.getPrice(), sent.getPaidPrice());
                        return payment;
                    });
            assertEquals("SUCCESS", service.createPayment(request()).getStatus());
            assertFalse(seat.isAvailable());
            verify(tickets).save(any());
            verify(seats).save(seat);
        }
    }
}
