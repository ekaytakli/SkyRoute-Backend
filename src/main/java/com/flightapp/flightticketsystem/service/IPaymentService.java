package com.flightapp.flightticketsystem.service;

import com.flightapp.flightticketsystem.dto.PaymentRequestDto;
import com.flightapp.flightticketsystem.dto.PaymentResponseDto;

/*
 * Ödeme işlemlerinin servis katmanında
 * kullanılacak metodunu tanımlar.
 */
public interface IPaymentService {

    // Gelen ödeme bilgilerine göre ödeme işlemini gerçekleştirir.
    PaymentResponseDto createPayment(
            PaymentRequestDto requestDto
    );
}