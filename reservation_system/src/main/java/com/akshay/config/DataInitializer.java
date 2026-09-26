package com.akshay.config;

import com.akshay.entity.ReservationStatus;
import com.akshay.entity.Resource;
import com.akshay.entity.Role;
import com.akshay.entity.User;
import com.akshay.repository.ReservationStatusRepository;
import com.akshay.repository.ResourceRepository;
import com.akshay.repository.RoleRepository;
import com.akshay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final ReservationStatusRepository reservationStatusRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Starting database initialization and seeding...");

        // 1. Seed Roles
        Role adminRole = roleRepository.findByName("ADMIN").orElseGet(() ->
                roleRepository.save(Role.builder()
                        .name("ADMIN")
                        .description("Administrator with full privileges")
                        .build())
        );

        Role userRole = roleRepository.findByName("USER").orElseGet(() ->
                roleRepository.save(Role.builder()
                        .name("USER")
                        .description("Standard user role")
                        .build())
        );

        // 2. Seed Reservation Statuses
        reservationStatusRepository.findByName("PENDING").orElseGet(() ->
                reservationStatusRepository.save(ReservationStatus.builder()
                        .name("PENDING")
                        .description("Reservation is awaiting confirmation or execution")
                        .build())
        );

        reservationStatusRepository.findByName("COMPLETED").orElseGet(() ->
                reservationStatusRepository.save(ReservationStatus.builder()
                        .name("COMPLETED")
                        .description("Reservation has been completed")
                        .build())
        );

        reservationStatusRepository.findByName("CANCELLED").orElseGet(() ->
                reservationStatusRepository.save(ReservationStatus.builder()
                        .name("CANCELLED")
                        .description("Reservation was cancelled")
                        .build())
        );

        // 3. Seed Admin User
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(adminRole)
                    .build();
            userRepository.save(admin);
            log.info("Seeded default admin user: admin / Admin@123");
        }

        // 4. Seed Standard User
        if (!userRepository.existsByUsername("user1")) {
            User user1 = User.builder()
                    .username("user1")
                    .email("user1@example.com")
                    .password(passwordEncoder.encode("User@123"))
                    .role(userRole)
                    .build();
            userRepository.save(user1);
            log.info("Seeded default standard user: user1 / User@123");
        }

        // 5. Seed Sample Resources
        if (resourceRepository.count() == 0) {
            resourceRepository.save(Resource.builder()
                    .name("Conference Room A")
                    .description("Spacious conference room with 4K projector, sound system, and seating for 20")
                    .basePrice(new BigDecimal("50.00"))
                    .available(true)
                    .build());

            resourceRepository.save(Resource.builder()
                    .name("Executive Boardroom")
                    .description("Premium executive boardroom with video conferencing suite and seating for 10")
                    .basePrice(new BigDecimal("120.00"))
                    .available(true)
                    .build());

            resourceRepository.save(Resource.builder()
                    .name("Dedicated Workstation Pod 1")
                    .description("Ergonomic workstation in quiet development zone with dual 4K monitors")
                    .basePrice(new BigDecimal("15.00"))
                    .available(true)
                    .build());

            log.info("Seeded initial sample resources");
        }

        log.info("Database initialization completed successfully!");
    }
}
