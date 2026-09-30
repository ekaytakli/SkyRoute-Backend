package com.flightapp.flightticketsystem.service;

import com.flightapp.flightticketsystem.dto.SeatRequestDto;
import com.flightapp.flightticketsystem.dto.SeatResponseDto;

import java.util.List;

public interface ISeatService {
    SeatResponseDto addSeat(SeatRequestDto requestDto);
    List<SeatResponseDto> getSeatsByFlightId(Integer flightId);
    List<SeatResponseDto> getAvailableSeatsByFlightId(Integer flightId);
    SeatResponseDto updateSeat(Integer id, SeatRequestDto requestDto);
    void deleteSeat(Integer id);
}
