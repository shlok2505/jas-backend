package com.jas.sarees.saree;

import java.math.BigDecimal;
import java.time.Instant;

// Everything the owner needs to manage a listing, including the supplier number and hidden ones.
// Only returned from /api/admin/sarees, which requires the ADMIN role.
public record AdminSareeResponse(
        Long id,
        String type,
        BigDecimal price,
        String imageUrl,
        String title,
        String description,
        String supplierWhatsapp,
        String status,
        Instant createdAt
) {
    public static AdminSareeResponse from(Saree s) {
        return new AdminSareeResponse(s.getId(), s.getType(), s.getPrice(), s.getImageUrl(),
                s.getTitle(), s.getDescription(), s.getSupplierWhatsapp(),
                s.getStatus().name(), s.getCreatedAt());
    }
}
