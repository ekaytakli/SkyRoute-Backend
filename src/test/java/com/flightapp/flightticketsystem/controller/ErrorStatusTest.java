package com.flightapp.flightticketsystem.controller;

import com.flightapp.flightticketsystem.exception.*;
import com.flightapp.flightticketsystem.service.IFlightService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ErrorStatusTest {
    private final IFlightService service = mock(IFlightService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new FlightController(service))
            .setControllerAdvice(new GlobalExceptionHandler(new StaticMessageSource())).build();

    @ParameterizedTest
    @CsvSource({"error.flight_not_found,404", "error.user_already_exists,409",
            "error.seat_not_available,409", "error.unauthorized,401", "error.seat_not_belong_to_flight,400"})
    void businessErrorsUseAppropriateStatus(String key, int status) throws Exception {
        when(service.getFlightById(1)).thenThrow(new BaseException(key));
        mvc.perform(get("/rest/api/flight/flights-1"))
                .andExpect(status().is(status)).andExpect(jsonPath("$.status").value(status));
    }

    @Test
    void forbiddenIsNotConvertedToServerError() throws Exception {
        when(service.getFlightById(1)).thenThrow(new AccessDeniedException("denied"));
        mvc.perform(get("/rest/api/flight/flights-1")).andExpect(status().isForbidden());
    }

    @Test
    void databaseConflictDoesNotExposeDetails() throws Exception {
        when(service.getFlightById(1)).thenThrow(new DataIntegrityViolationException("private SQL"));
        mvc.perform(get("/rest/api/flight/flights-1"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("error.data_conflict"));
    }

    @Test
    void malformedJsonReturnsBadRequest() throws Exception {
        mvc.perform(post("/rest/api/flight/add-flight").contentType("application/json").content("{"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
