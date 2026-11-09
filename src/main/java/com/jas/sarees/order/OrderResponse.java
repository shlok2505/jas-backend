package com.jas.sarees.order;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        Long id,
        Long sareeId,
        String sareeTitle,
        BigDecimal price,
        String status,
        Instant createdAt
) {
    public static OrderResponse from(SareeOrder o) {
        return new OrderResponse(o.getId(), o.getSaree().getId(), o.getSaree().getTitle(),
                o.getPriceAtOrder(), o.getStatus().name(), o.getCreatedAt());
    }
}