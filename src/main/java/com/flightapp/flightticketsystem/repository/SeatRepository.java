package com.flightapp.flightticketsystem.repository;

import com.flightapp.flightticketsystem.entities.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {
    List<Seat> findByFlightId(Integer flightId);
    List<Seat> findByFlightIdAndIsAvailable(Integer flightId, boolean isAvailable);
}
