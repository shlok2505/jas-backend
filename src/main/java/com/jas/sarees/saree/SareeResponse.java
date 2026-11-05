package com.jas.sarees.saree;

import java.math.BigDecimal;

public record SareeResponse(
        Long id,
        String type,
        BigDecimal price,
        String imageUrl,
        String title,
        String description
) {
    public static SareeResponse from(Saree s) {
        return new SareeResponse(
                s.getId(), s.getType(), s.getPrice(),
                s.getImageUrl(), s.getTitle(), s.getDescription());
    }
}
