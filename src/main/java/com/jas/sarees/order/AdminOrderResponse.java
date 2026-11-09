package com.jas.sarees.order;

import java.math.BigDecimal;
import java.time.Instant;

public record AdminOrderResponse(
        Long id,
        Long sareeId,
        String sareeTitle,
        String sareeImageUrl,
        BigDecimal price,
        String customerName,
        String customerPhone,
        String address,
        String pincode,
        String supplierWhatsapp,
        String whatsappLink,
        String status,
        Instant createdAt
) {
    public static AdminOrderResponse from(SareeOrder o, String whatsappLink) {
        return new AdminOrderResponse(o.getId(), o.getSaree().getId(), o.getSaree().getTitle(),
                o.getSaree().getImageUrl(), o.getPriceAtOrder(), o.getCustomerName(),
                o.getCustomerPhone(), o.getAddress(), o.getPincode(),
                o.getSaree().getSupplierWhatsapp(), whatsappLink,
                o.getStatus().name(), o.getCreatedAt());
    }
}