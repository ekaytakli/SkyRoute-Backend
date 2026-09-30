package com.flightapp.flightticketsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Bilet kesildikten sonra veya "Biletlerim" sayfasında kullanıcıya gösterilen
 * bilet detay modelidir.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketResponseDto {

    // Biletin veritabanındaki ID numarası
    private Integer id;

    // Yolcunun bilet kontrolünde ve check-in yaparken kullanacağı 6 haneli PNR kodu
    private String pnrKodu;

    // Biletin satın alındığı tarih ve saat bilgisi
    private LocalDateTime date;

    // Uçuşun kimlik numarası
    private Integer flightId;

    // Koltuğun kimlik numarası
    private Integer seatId;

    // Biletin ait olduğu koltuk etiketi (örn: "14F")
    private String seatNumber;
}