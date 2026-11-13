package com.flightapp.flightticketsystem.service;

import com.flightapp.flightticketsystem.entities.*;
import com.flightapp.flightticketsystem.exception.BaseException;
import com.flightapp.flightticketsystem.dto.TicketRequestDto;
import com.flightapp.flightticketsystem.repository.*;
import com.flightapp.flightticketsystem.service.impl.TicketServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(TicketServiceImpl.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class BookingConcurrencyTest {
    @Autowired FlightRepository flights;
    @Autowired SeatRepository seats;
    @Autowired TicketRepository tickets;
    @Autowired UserRepository users;
    @Autowired TicketServiceImpl service;
    @Autowired PlatformTransactionManager manager;

    @Test
    void simultaneousBookingsProduceOnlyOneTicket() throws Exception {
        Flight flight = flights.save(new Flight());
        Seat seat = new Seat();
        seat.setFlight(flight);
        seat.setSeatNumber("1A");
        seat.setAvailable(true);
        seat = seats.save(seat);
        TicketRequestDto request = new TicketRequestDto(flight.getId(), seat.getId());
        long before = tickets.count();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        Callable<Boolean> book = () -> {
            assertTrue(start.await(5, TimeUnit.SECONDS));
            try {
                service.createTicket(request);
                return true;
            } catch (BaseException ex) {
                assertEquals("error.seat_not_available", ex.getMessageKey());
                return false;
            }
        };
        try {
            Future<Boolean> first = pool.submit(book);
            Future<Boolean> second = pool.submit(book);
            start.countDown();
            assertNotEquals(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS));
            assertEquals(before + 1, tickets.count());
            assertFalse(seats.findById(seat.getId()).orElseThrow().isAvailable());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void duplicateSeatNumberInSameFlightIsRejected() {
        Flight flight = flights.save(new Flight());
        Seat first = new Seat();
        first.setFlight(flight);
        first.setSeatNumber("2A");
        seats.saveAndFlush(first);
        Seat duplicate = new Seat();
        duplicate.setFlight(flight);
        duplicate.setSeatNumber("2A");
        assertThrows(DataIntegrityViolationException.class, () -> seats.saveAndFlush(duplicate));
        Flight other = flights.save(new Flight());
        duplicate.setFlight(other);
        duplicate.setId(null);
        assertDoesNotThrow(() -> seats.saveAndFlush(duplicate));
    }

    @Test
    void duplicateEmailsAreRejected() {
        User user = new User();
        user.setEmail("duplicate@example.com");
        user.setPassword("test-hash");
        users.saveAndFlush(user);
        User duplicate = new User();
        duplicate.setEmail(user.getEmail());
        duplicate.setPassword("test-hash");
        assertThrows(DataIntegrityViolationException.class, () -> users.saveAndFlush(duplicate));
    }

    @Test
    void duplicatePnrAndSecondTicketForSameSeatAreRejected() {
        Flight flight = flights.save(new Flight());
        Seat first = new Seat();
        first.setFlight(flight);
        first.setSeatNumber("4A");
        first = seats.save(first);
        Seat second = new Seat();
        second.setFlight(flight);
        second.setSeatNumber("4B");
        second = seats.save(second);
        Ticket ticket = new Ticket();
        ticket.setFlight(flight);
        ticket.setSeat(first);
        ticket.setPnrKodu("PNR001");
        tickets.saveAndFlush(ticket);
        Ticket duplicatePnr = new Ticket();
        duplicatePnr.setFlight(flight);
        duplicatePnr.setSeat(second);
        duplicatePnr.setPnrKodu("PNR001");
        assertThrows(DataIntegrityViolationException.class, () -> tickets.saveAndFlush(duplicatePnr));
        Ticket duplicateSeat = new Ticket();
        duplicateSeat.setFlight(flight);
        duplicateSeat.setSeat(first);
        duplicateSeat.setPnrKodu("PNR002");
        assertThrows(DataIntegrityViolationException.class, () -> tickets.saveAndFlush(duplicateSeat));
    }

    @Test
    void ticketAndSeatChangesRollbackTogether() {
        Flight flight = flights.save(new Flight());
        Seat seat = new Seat();
        seat.setFlight(flight);
        seat.setSeatNumber("3A");
        seat.setAvailable(true);
        Seat saved = seats.save(seat);
        long before = tickets.count();
        assertThrows(IllegalStateException.class, () -> new TransactionTemplate(manager).execute(status -> {
            service.createTicket(new TicketRequestDto(flight.getId(), saved.getId()));
            throw new IllegalStateException("Simulated failure before commit");
        }));
        assertEquals(before, tickets.count());
        assertTrue(seats.findById(saved.getId()).orElseThrow().isAvailable());
    }
}
