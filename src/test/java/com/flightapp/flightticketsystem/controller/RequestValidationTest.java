package com.flightapp.flightticketsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flightapp.flightticketsystem.exception.GlobalExceptionHandler;
import com.flightapp.flightticketsystem.service.IFlightService;
import com.flightapp.flightticketsystem.service.IPaymentService;
import com.flightapp.flightticketsystem.service.ISeatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RequestValidationTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private IFlightService flights;
    private ISeatService seats;
    private IPaymentService payments;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        flights = mock(IFlightService.class);
        seats = mock(ISeatService.class);
        payments = mock(IPaymentService.class);
        mvc = MockMvcBuilders.standaloneSetup(new FlightController(flights),
                        new SeatController(seats), new PaymentController(payments))
                .setControllerAdvice(new GlobalExceptionHandler(new StaticMessageSource()))
                .build();
    }

    @ParameterizedTest
    @CsvSource({
            "2030-12-01 12:00,2030-12-01 11:00",
            "2030-12-01 12:00,2030-12-01 12:00",
            "2030-02-30 12:00,2030-03-01 13:00",
            "2030-12-01T12:00,2030-12-01 13:00",
            "2030-12-01 12:00,not-a-date",
            "'',2030-12-01 13:00"
    })
    void invalidFlightScheduleIsRejectedOnCreateAndUpdate(String departure, String arrival) throws Exception {
        ObjectNode request = flight();
        request.put("departureTime", departure);
        request.put("destinationTime", arrival);
        send(post("/rest/api/flight/add-flight"), request, 400);
        send(put("/rest/api/flight/save-flight-1"), request, 400);
        verifyNoInteractions(flights);
    }

    @Test
    void missingFlightDateIsRejected() throws Exception {
        ObjectNode request = flight();
        request.remove("departureTime");
        send(post("/rest/api/flight/add-flight"), request, 400);
        verifyNoInteractions(flights);
    }

    @Test
    void validOvernightFlightCanBeCreatedAndUpdated() throws Exception {
        send(post("/rest/api/flight/add-flight"), flight(), 200);
        send(put("/rest/api/flight/save-flight-1"), flight(), 200);
        verify(flights).addFlight(any());
        verify(flights).updateFlight(eq(1), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "null"})
    void invalidSeatPriceIsRejectedOnCreateAndUpdate(String price) throws Exception {
        ObjectNode request = seat();
        request.set("price", mapper.readTree(price));
        send(post("/api/v1/seats/add/seats"), request, 400);
        send(put("/api/v1/seats/update/seats/1"), request, 400);
        verifyNoInteractions(seats);
    }

    @Test
    void positiveSeatPriceIsAccepted() throws Exception {
        send(post("/api/v1/seats/add/seats"), seat(), 200);
        verify(seats).addSeat(any());
    }

    @ParameterizedTest
    @CsvSource({"flightId,0", "seatId,-1", "firstName,''", "lastName,''",
            "email,invalid", "cardHolderName,''", "cardNumber,abc",
            "expireMonth,13", "expireYear,30", "cvc,12"})
    void invalidPaymentFieldsAreRejected(String field, String value) throws Exception {
        ObjectNode request = payment();
        if (field.endsWith("Id")) {
            request.put(field, Integer.parseInt(value));
        } else {
            request.put(field, value);
        }
        send(post("/api/v1/payments"), request, 400);
        verifyNoInteractions(payments);
    }

    @ParameterizedTest
    @ValueSource(strings = {"flightId", "seatId", "firstName", "lastName", "email",
            "cardHolderName", "cardNumber", "expireMonth", "expireYear", "cvc"})
    void missingPaymentFieldsAreRejected(String field) throws Exception {
        ObjectNode request = payment();
        request.remove(field);
        send(post("/api/v1/payments"), request, 400);
        verifyNoInteractions(payments);
    }

    @Test
    void validPaymentReachesService() throws Exception {
        send(post("/api/v1/payments"), payment(), 200);
        verify(payments).createPayment(any());
    }

    private void send(MockHttpServletRequestBuilder request, ObjectNode body, int expected) throws Exception {
        mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(body.toString()))
                .andExpect(status().is(expected));
    }

    private ObjectNode flight() throws Exception {
        return (ObjectNode) mapper.readTree("""
                {"flightNo":"TK101","departurePoint":"Istanbul","destinationPoint":"Ankara",
                 "departureTime":"2030-12-01 23:00","destinationTime":"2030-12-02 01:00"}
                """);
    }

    private ObjectNode seat() throws Exception {
        return (ObjectNode) mapper.readTree("""
                {"seatNumber":"1A","seatType":"ECONOMY","available":true,"price":100.50,"flightId":1}
                """);
    }

    private ObjectNode payment() throws Exception {
        return (ObjectNode) mapper.readTree("""
                {"flightId":1,"seatId":1,"firstName":"Test","lastName":"User","email":"test@example.com",
                 "cardHolderName":"Test User","cardNumber":"5528790000000008",
                 "expireMonth":"12","expireYear":"2030","cvc":"123"}
                """);
    }
}
