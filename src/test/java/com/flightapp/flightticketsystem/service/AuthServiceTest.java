package com.flightapp.flightticketsystem.service;

import com.flightapp.flightticketsystem.entities.Role;
import com.flightapp.flightticketsystem.entities.User;
import com.flightapp.flightticketsystem.exception.BaseException;
import com.flightapp.flightticketsystem.jwt.LoginRequest;
import com.flightapp.flightticketsystem.jwt.RegisterRequest;
import com.flightapp.flightticketsystem.jwt.AuthResponse;
import com.flightapp.flightticketsystem.jwt.JwtService;
import com.flightapp.flightticketsystem.repository.RoleRepository;
import com.flightapp.flightticketsystem.repository.UserRepository;
import com.flightapp.flightticketsystem.service.impl.AuthService;
import com.flightapp.flightticketsystem.mapper.UserMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    @Test
    public void register_WhenUserIsNew_ShouldReturnAuthResponse(){
        RegisterRequest request = new RegisterRequest();
        request.setEmail("asrin@icloud.com");
        request.setPassword("12345");
        request.setFirstName("Asrın Deniz");
        request.setLastName("Ak");

        Role customerRole = new Role();
        customerRole.setRoleName("Customer");

        User mockUser = mock(User.class);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userMapper.toEntity(any(RegisterRequest.class))).thenReturn(mockUser);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed_password");
        when(roleRepository.findByRoleName("Customer")).thenReturn(Optional.of(customerRole));
        when(jwtService.generateToken(any(User.class))).thenReturn("sahte_token");

        AuthResponse response = authService.register(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("sahte_token", response.getToken());

        verify(userRepository, times(1)).save(any(User.class));
    }


    @Test
    public void register_WhenUserAlreadyExists_ShouldThrowException(){
        RegisterRequest request = new RegisterRequest();
        request.setEmail("asrin@icloud.com");
        request.setPassword("12345");
        request.setFirstName("Asrın Deniz");
        request.setLastName("Ak");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        Assertions.assertThrows(BaseException.class, () -> {
            authService.register(request);
        });

        verify(userRepository, times(0)).save(any(User.class));
    }


    @Test
    public void login_WhenUserIsValid_ShouldReturnAuthResponse(){
        LoginRequest request = new LoginRequest();
        request.setEmail("asrin@icloud.com");
        request.setPassword("12345");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));
        when(jwtService.generateToken(any(User.class))).thenReturn("sahte_token");

        AuthResponse response = authService.login(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("sahte_token", response.getToken());

        verify(userRepository, times(1)).findByEmail(request.getEmail());
    }

    @Test
    public void login_WhenUserIsInvalid_ShouldThrowException(){
        LoginRequest request = new LoginRequest();
        request.setEmail("asrin@icloud.com");
        request.setPassword("12345");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        Assertions.assertThrows(BaseException.class, () -> {
            authService.login(request);
        });

        verify(jwtService, times(0)).generateToken(any());
    }

    @Test
    public void login_WhenPasswordIsWrong_ShouldThrowException(){
        LoginRequest request = new LoginRequest();
        request.setEmail("asrin@icloud.com");
        request.setPassword("12345");

        when(authenticationManager.authenticate(any())).thenThrow(new RuntimeException("Bad credentials"));

        Assertions.assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });

        verify(userRepository, times(0)).findByEmail(request.getEmail());
    }
}