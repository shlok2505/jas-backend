package com.jas.sarees.saree;

import java.math.BigDecimal;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record SareeRequest(
        @NotBlank(message = "type is required")
        String type,

        @NotNull(message = "price is required")
        @Positive(message = "price must be more than 0")
        BigDecimal price,

        @NotBlank(message = "supplier WhatsApp number is required")
        @Pattern(regexp = "\\d{10,15}", message = "use digits only, with country code, e.g. 919876543210")
                @NotBlank(message = "supplier WhatsApp number is required")
        @Pattern(regexp = "\\d{10,15}", message = "use digits only, with country code, e.g. 919876543210")
        String supplierWhatsapp,

        @NotNull(message = "photo is required")
        MultipartFile photo

) {}
