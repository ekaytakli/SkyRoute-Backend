package com.flightapp.flightticketsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Uçuş sorgulama ve listeleme işlemlerinde istemciye dönülen veri modeli.
 * Entity üzerindeki ilişkisel karmaşık veriler elenerek sadece temel uçuş bilgisi iletilir.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlightResponse {

    // Veritabanında oluşan benzersiz uçuş ID'si
    private Integer id;

    // Uçuş numarası (örn: TK1923)
    private String flightNo;

    // Kalkış yeri
    private String departurePoint;

    // Varış yeri
    private String destinationPoint;

    // Planlanan kalkış zamanı
    private String departureTime;

    // Planlanan varış zamanı
    private String destinationTime;
}