package com.akshay.controller;

import com.akshay.dto.common.ApiResponse;
import com.akshay.dto.common.PageResponse;
import com.akshay.dto.reservation.ReservationCreateRequest;
import com.akshay.dto.reservation.ReservationResponse;
import com.akshay.dto.reservation.ReservationStatusUpdateRequest;
import com.akshay.security.CustomUserDetails;
import com.akshay.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservation Management", description = "Endpoints for creating, querying, filtering, and managing reservations")
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "Create reservation", description = "Creates a reservation for the authenticated user and calculates total price")
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @Valid @RequestBody ReservationCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        ReservationResponse created = reservationService.createReservation(request, currentUser);
        return new ResponseEntity<>(
                ApiResponse.success("Reservation created successfully", created),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    @Operation(summary = "Get reservations (Filtered & Paginated)", description = "Retrieves reservations with dynamic filtering by status, price range, with pagination and sorting. Regular users only see their own reservations.")
    public ResponseEntity<ApiResponse<PageResponse<ReservationResponse>>> getReservations(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        Sort.Direction direction = Sort.Direction.DESC;
        String sortProperty = "createdAt";

        if (sort.length > 0) {
            String[] sortParts = sort[0].split(",");
            sortProperty = sortParts[0];
            if (sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1])) {
                direction = Sort.Direction.ASC;
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortProperty));
        PageResponse<ReservationResponse> response = reservationService.getReservations(
                status, minPrice, maxPrice, pageable, currentUser);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation by ID", description = "Retrieves details of a specific reservation")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservationById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        ReservationResponse response = reservationService.getReservationById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update reservation status", description = "Updates reservation status. Regular users can only cancel their own pending reservation; admins can update to any status.")
    public ResponseEntity<ApiResponse<ReservationResponse>> updateReservationStatus(
            @PathVariable Long id,
            @Valid @RequestBody ReservationStatusUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        ReservationResponse response = reservationService.updateReservationStatus(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Reservation status updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Delete reservation (Admin only)", description = "Deletes a reservation by ID")
    public ResponseEntity<ApiResponse<Void>> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.ok(ApiResponse.success("Reservation deleted successfully", null));
    }
}
