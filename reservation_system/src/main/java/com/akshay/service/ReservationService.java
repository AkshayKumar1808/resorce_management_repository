package com.akshay.service;

import com.akshay.dto.common.PageResponse;
import com.akshay.dto.reservation.ReservationCreateRequest;
import com.akshay.dto.reservation.ReservationResponse;
import com.akshay.dto.reservation.ReservationStatusUpdateRequest;
import com.akshay.security.CustomUserDetails;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ReservationService {
    ReservationResponse createReservation(ReservationCreateRequest request, CustomUserDetails currentUser);
    ReservationResponse getReservationById(Long id, CustomUserDetails currentUser);
    PageResponse<ReservationResponse> getReservations(
            String status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable,
            CustomUserDetails currentUser);
    ReservationResponse updateReservationStatus(Long id, ReservationStatusUpdateRequest request, CustomUserDetails currentUser);
    void deleteReservation(Long id);
}
