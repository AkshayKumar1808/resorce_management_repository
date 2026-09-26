package com.akshay.dto.resource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private Boolean available;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
