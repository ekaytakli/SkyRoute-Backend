package com.flightapp.flightticketsystem.service;

import com.flightapp.flightticketsystem.jwt.LoginRequest;
import com.flightapp.flightticketsystem.jwt.RegisterRequest;
import com.flightapp.flightticketsystem.jwt.AuthResponse;

public interface IAuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
