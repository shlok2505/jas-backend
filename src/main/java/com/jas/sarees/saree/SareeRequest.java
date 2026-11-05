package com.jas.sarees.saree;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record SareeRequest(
        @NotBlank(message = "type is required")
        String type,

        @NotNull(message = "price is required")
        @Positive(message = "price must be more than 0")
        BigDecimal price,

        @NotBlank(message = "supplier WhatsApp number is required")
        @Pattern(regexp = "\\d{10,15}", message = "use digits only, with country code, e.g. 919876543210")
        String supplierWhatsapp
) {}
