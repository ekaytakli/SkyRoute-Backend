package com.flightapp.flightticketsystem.repository;

import com.flightapp.flightticketsystem.entities.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.id = :id")
    Optional<Seat> findByIdForUpdate(@Param("id") Integer id);

    List<Seat> findByFlightId(Integer flightId);
    List<Seat> findByFlightIdAndIsAvailable(Integer flightId, boolean isAvailable);
}
