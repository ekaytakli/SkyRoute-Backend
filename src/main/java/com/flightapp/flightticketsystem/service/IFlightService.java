package com.flightapp.flightticketsystem.service;

import com.flightapp.flightticketsystem.dto.FlightRequest;
import com.flightapp.flightticketsystem.dto.FlightResponse;

import java.util.List;

public interface IFlightService {

    FlightResponse addFlight(FlightRequest flightRequest);

    List<FlightResponse> getAllFlights();

    FlightResponse getFlightById(Integer id);

    FlightResponse updateFlight(Integer id, FlightRequest flightRequest);

    void deleteFlight(Integer id);
}