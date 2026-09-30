package com.flightapp.flightticketsystem.mapper;

import com.flightapp.flightticketsystem.dto.FlightRequest;
import com.flightapp.flightticketsystem.dto.FlightResponse;
import com.flightapp.flightticketsystem.entities.Flight;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class FlightMapper {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public Flight toEntity(FlightRequest request){
        if (request == null) {
            return null;
        }

        Flight flight = new Flight();
        flight.setFlightNo(request.getFlightNo());
        flight.setDeparturePoint(request.getDeparturePoint());
        flight.setDestinationPoint(request.getDestinationPoint());

        if (request.getDepartureTime() != null) {
            flight.setDepartureTime(LocalDateTime.parse(request.getDepartureTime(), formatter));
        }
        if (request.getDestinationTime() != null) {
            flight.setDestinationTime(LocalDateTime.parse(request.getDestinationTime(), formatter));
        }

        return flight;
    }

    public FlightResponse toResponse(Flight flight){
        if(flight == null){
            return null;
        }

        return new FlightResponse(
                flight.getId(),
                flight.getFlightNo(),
                flight.getDeparturePoint(),
                flight.getDestinationPoint(),
                flight.getDepartureTime() != null ? flight.getDepartureTime().format(formatter) : null,
                flight.getDestinationTime() != null ? flight.getDestinationTime().format(formatter) : null
        );
    }
}