package com.flightapp.flightticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Bir uçuşa yeni bir koltuk tanımlanırken kullanılan veri transfer nesnesi.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeatRequestDto {

    // Koltuk numarası (örn: "1A", "12B") boş bırakılamaz.
    @NotBlank(message = "{error.seat_number_blank}")
    private String seatNumber;

    // Koltuk sınıfı (örn: "ECONOMY", "BUSINESS") boş bırakılamaz.
    @NotBlank(message = "{error.seat_type_blank}")
    private String seatType;

    // Koltuğun satışa açık olup olmadığı (true = boş, false = rezerve/dolu)
    private boolean isAvailable;

    // Koltuğun bilet satış ücreti (boş olamaz)
    @NotNull(message = "{error.price_null}")
    private BigDecimal price;

    // Koltuğun ait olduğu uçuşun referans ID'si (boş olamaz)
    @NotNull(message = "{error.flight_id_null}")
    private Integer flightId;
}