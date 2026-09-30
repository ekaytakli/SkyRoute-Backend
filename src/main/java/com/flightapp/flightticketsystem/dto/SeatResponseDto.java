package com.flightapp.flightticketsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Koltuk sorgulama ekranlarında (örn: uçak içi koltuk seçim haritası)
 * istemciye gösterilen koltuk bilgilerini taşır.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeatResponseDto {
    // Koltuğun veritabanındaki benzersiz ID'si
    private Integer id;

    // Koltuk numarası (örn: "3C")
    private String seatNumber;

    // Koltuğun sınıfı ("ECONOMY", "BUSINESS")
    private String seatType;

    // Koltuğun satın alınabilir olup olmadığı bilgisi
    private boolean isAvailable;

    // Koltuk ücreti
    private BigDecimal price;

    // Bağlı olduğu uçuşun ID'si
    private Integer flightId;
}