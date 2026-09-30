package com.flightapp.flightticketsystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Doğrudan bilet oluşturma isteği gönderildiğinde (örn: ödemesiz veya iç servis çağrıları)
 * gerekli minimum bilgileri (uçuş ve koltuk seçimi) taşır.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketRequestDto {

    // Biletin kesileceği uçuşun kimlik numarası (zorunlu alan)
    @NotNull
    private Integer flightId;

    // Satın alınmak istenen koltuğun kimlik numarası (zorunlu alan)
    @NotNull
    private Integer seatId;
}