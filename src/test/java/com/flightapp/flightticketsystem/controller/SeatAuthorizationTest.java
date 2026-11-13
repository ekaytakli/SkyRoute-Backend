package com.flightapp.flightticketsystem.controller;

import com.flightapp.flightticketsystem.dto.SeatRequestDto;
import com.flightapp.flightticketsystem.service.ISeatService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class SeatAuthorizationTest {
    private AnnotationConfigApplicationContext context;
    private SeatController controller;
    private ISeatService service;
    private final SeatRequestDto request = new SeatRequestDto();

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        context = new AnnotationConfigApplicationContext(TestConfig.class);
        controller = context.getBean(SeatController.class);
        service = context.getBean(ISeatService.class);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        context.close();
    }

    @Test
    void customerCannotAddOrUpdateSeats() {
        authenticate("ROLE_CUSTOMER");
        assertThrows(AccessDeniedException.class, () -> controller.addSeat(request));
        assertThrows(AccessDeniedException.class, () -> controller.updateSeat(1, request));
        verifyNoInteractions(service);
    }

    @Test
    void anonymousCannotAddOrUpdateSeats() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> controller.addSeat(request));
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> controller.updateSeat(1, request));
        verifyNoInteractions(service);
    }

    @Test
    void adminCanAddAndUpdateSeats() {
        authenticate("ROLE_ADMIN");
        controller.addSeat(request);
        controller.updateSeat(1, request);
        verify(service).addSeat(request);
        verify(service).updateSeat(1, request);
    }

    private void authenticate(String authority) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test", null,
                        List.of(new SimpleGrantedAuthority(authority))));
    }

    @Configuration
    @EnableMethodSecurity
    static class TestConfig {
        @Bean
        ISeatService seatService() {
            return mock(ISeatService.class);
        }

        @Bean
        SeatController seatController(ISeatService service) {
            return new SeatController(service);
        }
    }
}
