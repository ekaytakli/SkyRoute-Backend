package com.flightapp.flightticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Yeni bir uçuş oluştururken veya mevcut uçuşu güncellerken
 * istemciden (frontend/admin paneli) beklenen veri formatını tanımlar.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlightRequest {

    // Uçuş kodu boş bırakılamaz ve sadece boşluktan oluşamaz.
    @NotBlank(message = "{error.flight_no_blank}")
    private String flightNo;

    // Kalkış noktası (şehir/havalimanı) boş olamaz.
    @NotBlank(message = "{error.departure_blank}")
    private String departurePoint;

    // İniş/Varış noktası boş olamaz.
    @NotBlank(message = "{error.destination_blank}")
    private String destinationPoint;

    // Kalkış zamanı metin formatında (örn: "2026-10-15 14:30") zorunlu alandır.
    @NotNull(message = "{error.time_null}")
    private String departureTime;

    // Varış zamanı metin formatında zorunlu alandır.
    @NotNull(message = "{error.time_null}")
    private String destinationTime;
}