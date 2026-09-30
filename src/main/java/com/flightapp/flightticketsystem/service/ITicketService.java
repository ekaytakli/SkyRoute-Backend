package com.flightapp.flightticketsystem.service;

import com.flightapp.flightticketsystem.dto.TicketRequestDto;
import com.flightapp.flightticketsystem.dto.TicketResponseDto;

import java.util.List;

public interface ITicketService {

    TicketResponseDto createTicket(
            TicketRequestDto requestDto
    );

    List<TicketResponseDto> getMyTickets();
}