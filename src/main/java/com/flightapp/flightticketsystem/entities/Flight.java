package com.flightapp.flightticketsystem.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "flights")
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flight_id")
    private Integer id;

    @Column(name = "flight_no")
    private String flightNo;

    @Column(name = "departure_point")
    private String departurePoint;

    @Column(name = "destination_point")
    private String destinationPoint;

    @Column(name = "departure_time")
    private LocalDateTime departureTime;

    @Column(name = "destination_time")
    private LocalDateTime destinationTime;

    @OneToMany(mappedBy = "flight")
    private List<Seat> seats;

    @OneToMany(mappedBy = "flight")
    private List<Ticket> tickets ;

}
