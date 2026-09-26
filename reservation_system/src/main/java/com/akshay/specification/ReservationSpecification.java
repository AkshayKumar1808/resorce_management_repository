package com.akshay.specification;

import com.akshay.entity.Reservation;
import com.akshay.entity.ReservationStatus;
import com.akshay.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ReservationSpecification {

    public static Specification<Reservation> withFilters(
            String status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Long userId) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(status)) {
                Join<Reservation, ReservationStatus> statusJoin = root.join("status");
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(statusJoin.get("name")),
                        status.trim().toUpperCase()
                ));
            }

            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (userId != null) {
                Join<Reservation, User> userJoin = root.join("user");
                predicates.add(criteriaBuilder.equal(userJoin.get("id"), userId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
