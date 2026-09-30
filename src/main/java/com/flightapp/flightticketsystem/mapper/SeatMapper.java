package com.flightapp.flightticketsystem.mapper;

import com.flightapp.flightticketsystem.dto.SeatRequestDto;
import com.flightapp.flightticketsystem.dto.SeatResponseDto;
import com.flightapp.flightticketsystem.entities.Seat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SeatMapper {

    public Seat toEntity(SeatRequestDto requestDto) {
        if (requestDto == null) {
            return null;
        }
        Seat seat = new Seat();
        seat.setSeatNumber(requestDto.getSeatNumber());
        seat.setSeatType(requestDto.getSeatType());
        seat.setAvailable(requestDto.isAvailable());
        seat.setPrice(requestDto.getPrice());
        return seat;
    }

    public SeatResponseDto toDto(Seat seat) {
        if (seat == null) {
            return null;
        }
        return SeatResponseDto.builder()
                .id(seat.getId())
                .seatNumber(seat.getSeatNumber())
                .seatType(seat.getSeatType())
                .isAvailable(seat.isAvailable())
                .price(seat.getPrice())
                .flightId(seat.getFlight() != null ? seat.getFlight().getId() : null)
                .build();
    }

    public List<SeatResponseDto> toDtoList(List<Seat> seats) {
        if (seats == null) {
            return null;
        }

        List<SeatResponseDto> dtoList = new ArrayList<>();

        for (Seat seat : seats) {


            SeatResponseDto convertedDto = toDto(seat);


            dtoList.add(convertedDto);
        }

        return dtoList;
    }
    public void updateEntityFromDto(SeatRequestDto requestDto, Seat seat) {
        if (requestDto == null || seat == null) {
            return;
        }
        seat.setSeatNumber(requestDto.getSeatNumber());
        seat.setSeatType(requestDto.getSeatType());
        seat.setAvailable(requestDto.isAvailable());
        seat.setPrice(requestDto.getPrice());
    }
}

