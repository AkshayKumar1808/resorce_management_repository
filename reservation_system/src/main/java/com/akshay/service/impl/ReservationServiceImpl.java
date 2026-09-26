package com.akshay.service.impl;

import com.akshay.dto.common.PageResponse;
import com.akshay.dto.reservation.ReservationCreateRequest;
import com.akshay.dto.reservation.ReservationResponse;
import com.akshay.dto.reservation.ReservationStatusUpdateRequest;
import com.akshay.dto.resource.ResourceResponse;
import com.akshay.dto.user.UserResponse;
import com.akshay.entity.Reservation;
import com.akshay.entity.ReservationStatus;
import com.akshay.entity.Resource;
import com.akshay.entity.User;
import com.akshay.exception.BadRequestException;
import com.akshay.exception.ForbiddenException;
import com.akshay.exception.ResourceNotFoundException;
import com.akshay.repository.ReservationRepository;
import com.akshay.repository.ReservationStatusRepository;
import com.akshay.repository.ResourceRepository;
import com.akshay.repository.UserRepository;
import com.akshay.security.CustomUserDetails;
import com.akshay.service.ReservationService;
import com.akshay.specification.ReservationSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final ReservationStatusRepository reservationStatusRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReservationResponse createReservation(ReservationCreateRequest request, CustomUserDetails currentUser) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BadRequestException("Start time must be before end time");
        }

        long minutes = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        if (minutes < 15) {
            throw new BadRequestException("Reservation duration must be at least 15 minutes");
        }

        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + request.getResourceId()));

        if (!Boolean.TRUE.equals(resource.getAvailable())) {
            throw new BadRequestException("Resource '" + resource.getName() + "' is currently unavailable for reservations");
        }

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.getId()));

        ReservationStatus status = reservationStatusRepository.findByName("PENDING")
                .orElseThrow(() -> new ResourceNotFoundException("Default reservation status PENDING not found"));

        // Calculate price based on duration hours * basePrice
        BigDecimal hours = BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        BigDecimal totalPrice = resource.getBasePrice().multiply(hours).setScale(2, RoundingMode.HALF_UP);

        Reservation reservation = Reservation.builder()
                .user(user)
                .resource(resource)
                .status(status)
                .price(totalPrice)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);
        return mapToReservationResponse(savedReservation);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id, CustomUserDetails currentUser) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        boolean isAdmin = "ADMIN".equals(currentUser.getRole());
        if (!isAdmin && !reservation.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Access denied: You can only view your own reservations");
        }

        return mapToReservationResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReservationResponse> getReservations(
            String status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable,
            CustomUserDetails currentUser) {

        boolean isAdmin = "ADMIN".equals(currentUser.getRole());
        Long filterUserId = isAdmin ? null : currentUser.getId();

        Specification<Reservation> spec = ReservationSpecification.withFilters(
                status, minPrice, maxPrice, filterUserId);

        Page<Reservation> page = reservationRepository.findAll(spec, pageable);
        Page<ReservationResponse> responsePage = page.map(this::mapToReservationResponse);

        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional
    public ReservationResponse updateReservationStatus(Long id, ReservationStatusUpdateRequest request, CustomUserDetails currentUser) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        boolean isAdmin = "ADMIN".equals(currentUser.getRole());
        String requestedStatusName = request.getStatus().trim().toUpperCase();

        if (!isAdmin) {
            if (!reservation.getUser().getId().equals(currentUser.getId())) {
                throw new ForbiddenException("Access denied: You can only update your own reservations");
            }
            if (!"CANCELLED".equals(requestedStatusName)) {
                throw new BadRequestException("Regular users can only cancel their reservations");
            }
            if (!"PENDING".equals(reservation.getStatus().getName())) {
                throw new BadRequestException("Only PENDING reservations can be cancelled");
            }
        }

        ReservationStatus newStatus = reservationStatusRepository.findByName(requestedStatusName)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation status not found: " + requestedStatusName));

        reservation.setStatus(newStatus);
        Reservation updated = reservationRepository.save(reservation);
        return mapToReservationResponse(updated);
    }

    @Override
    @Transactional
    public void deleteReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Reservation not found with id: " + id);
        }
        reservationRepository.deleteById(id);
    }

    private ReservationResponse mapToReservationResponse(Reservation reservation) {
        UserResponse userResponse = UserResponse.builder()
                .id(reservation.getUser().getId())
                .username(reservation.getUser().getUsername())
                .email(reservation.getUser().getEmail())
                .role(reservation.getUser().getRole().getName())
                .createdAt(reservation.getUser().getCreatedAt())
                .updatedAt(reservation.getUser().getUpdatedAt())
                .build();

        ResourceResponse resourceResponse = ResourceResponse.builder()
                .id(reservation.getResource().getId())
                .name(reservation.getResource().getName())
                .description(reservation.getResource().getDescription())
                .basePrice(reservation.getResource().getBasePrice())
                .available(reservation.getResource().getAvailable())
                .createdAt(reservation.getResource().getCreatedAt())
                .updatedAt(reservation.getResource().getUpdatedAt())
                .build();

        return ReservationResponse.builder()
                .id(reservation.getId())
                .user(userResponse)
                .resource(resourceResponse)
                .status(reservation.getStatus().getName())
                .price(reservation.getPrice())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }
}
