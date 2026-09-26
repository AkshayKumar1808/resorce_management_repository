package com.akshay.dto.reservation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationStatusUpdateRequest {

    @NotBlank(message = "Status name is required")
    @Pattern(regexp = "^(PENDING|COMPLETED|CANCELLED)$", message = "Status must be PENDING, COMPLETED, or CANCELLED")
    private String status;
}
