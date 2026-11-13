package com.flightapp.flightticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.AssertTrue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
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
    @NotBlank(message = "{error.time_null}")
    private String departureTime;

    // Varış zamanı metin formatında zorunlu alandır.
    @NotBlank(message = "{error.time_null}")
    private String destinationTime;

    @JsonIgnore
    @AssertTrue(message = "{error.flight_schedule_invalid}")
    public boolean isScheduleValid() {
        // Eksik alanlari @NotBlank kontrol eder. Burada tarih bicimi ve sirasi denetlenir.
        if (departureTime == null || departureTime.isBlank()
                || destinationTime == null || destinationTime.isBlank()) {
            return true;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm")
                .withResolverStyle(ResolverStyle.STRICT);
        try {
            LocalDateTime departure = LocalDateTime.parse(departureTime, formatter);
            LocalDateTime destination = LocalDateTime.parse(destinationTime, formatter);
            return destination.isAfter(departure);
        } catch (DateTimeParseException ex) {
            return false;
        }
    }
}
