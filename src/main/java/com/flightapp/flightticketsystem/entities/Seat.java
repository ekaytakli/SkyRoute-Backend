package com.flightapp.flightticketsystem.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Integer id;

    @Column(name = "seat_number")
    private String seatNumber;

    @Column(name = "seat_type")
    private String seatType;

    @Column(name = "is_available")
    private boolean isAvailable;

    @Column(name = "price")
    private java.math.BigDecimal price;

    @OneToOne(mappedBy = "seat")
    private Ticket ticket;

    @ManyToOne
    @JoinColumn(name = "flight_id")
    private Flight flight;
}

