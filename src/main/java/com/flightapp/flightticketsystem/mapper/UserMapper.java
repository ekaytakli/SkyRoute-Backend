package com.flightapp.flightticketsystem.mapper;

import com.flightapp.flightticketsystem.dto.DtoUsers;
import com.flightapp.flightticketsystem.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public DtoUsers toDto(User user) {
        if (user == null) {
            return null;
        }
        return new DtoUsers(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }

    public User toEntity(DtoUsers dto) {
        if (dto == null) {
            return null;
        }
        User user = new User();
        user.setId(dto.getId());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        return user;
    }

    public User toEntity(com.flightapp.flightticketsystem.jwt.RegisterRequest request) {
        if (request == null) {
            return null;
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        return user;
    }
}
