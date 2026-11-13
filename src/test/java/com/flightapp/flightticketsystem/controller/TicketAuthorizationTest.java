package com.flightapp.flightticketsystem.controller;

import com.flightapp.flightticketsystem.dto.TicketRequestDto;
import com.flightapp.flightticketsystem.service.ITicketService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TicketAuthorizationTest {
    private AnnotationConfigApplicationContext context;
    private TicketController controller;
    private ITicketService service;
    private final TicketRequestDto request = new TicketRequestDto(3, 12);

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        context = new AnnotationConfigApplicationContext(TestConfig.class);
        controller = context.getBean(TicketController.class);
        service = context.getBean(ITicketService.class);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        context.close();
    }

    @Test
    void customerCannotCreateUnpaidTicket() {
        authenticate("ROLE_CUSTOMER");
        assertThrows(AccessDeniedException.class, () -> controller.createTicket(request));
        verifyNoInteractions(service);
    }

    @Test
    void guestCannotCreateUnpaidTicket() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "test", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
        assertThrows(AccessDeniedException.class, () -> controller.createTicket(request));
        verifyNoInteractions(service);
    }

    @Test
    void adminCanCreateUnpaidTicket() {
        authenticate("ROLE_ADMIN");
        controller.createTicket(request);
        verify(service).createTicket(request);
    }

    @Test
    void customerCanStillListOwnTickets() {
        authenticate("ROLE_CUSTOMER");
        controller.getMyTickets();
        verify(service).getMyTickets();
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
        ITicketService ticketService() {
            return mock(ITicketService.class);
        }

        @Bean
        TicketController ticketController(ITicketService service) {
            return new TicketController(service);
        }
    }
}
