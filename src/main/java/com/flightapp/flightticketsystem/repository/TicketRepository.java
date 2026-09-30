package com.flightapp.flightticketsystem.repository;

import com.flightapp.flightticketsystem.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository
        extends JpaRepository<Ticket, Integer> {

    List<Ticket> findByUserId(Integer userId);
}