package com.flightapp.flightticketsystem.config;

import com.flightapp.flightticketsystem.jwt.JwtService;
import com.flightapp.flightticketsystem.repository.RoleRepository;
import com.flightapp.flightticketsystem.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class SecurityDefaultsTest {
    @Test
    void missingOrWeakJwtKeyPreventsStartup() {
        new ApplicationContextRunner().withUserConfiguration(JwtService.class)
                .run(context -> assertThat(context).hasFailed());
        new ApplicationContextRunner().withUserConfiguration(JwtService.class)
                .withPropertyValues("jwt.secret=c2hvcnQ=")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void validKeyCanStartJwtService() {
        new ApplicationContextRunner().withUserConfiguration(JwtService.class)
                .withPropertyValues("jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=")
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void defaultStartupDoesNotCreateAdminAccount() throws Exception {
        UserRepository users = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        DatabaseSeeder seeder = new DatabaseSeeder(mock(RoleRepository.class), users, encoder);
        seeder.run();
        verifyNoInteractions(users, encoder);
    }

    @Test
    void enabledBootstrapRejectsMissingCredentials() {
        DatabaseSeeder seeder = new DatabaseSeeder(mock(RoleRepository.class), mock(UserRepository.class),
                mock(PasswordEncoder.class));
        ReflectionTestUtils.setField(seeder, "adminEnabled", true);
        ReflectionTestUtils.setField(seeder, "adminEmail", "");
        ReflectionTestUtils.setField(seeder, "adminPassword", "");
        assertThrows(IllegalStateException.class, seeder::run);
    }
}
