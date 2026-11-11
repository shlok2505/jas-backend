package com.jas.sarees.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OrderRequest(
        @NotNull(message = "sareeId is required")
        Long sareeId,

        @NotBlank(message = "name is required")
        @Size(min = 2, max = 100, message = "name must be 2 to 100 characters")
        String customerName,

        @NotBlank(message = "phone is required")
        @Pattern(regexp = "[6-9]\\d{9}", message = "enter a 10-digit Indian mobile number")
        String customerPhone,

        @NotBlank(message = "address is required")
        @Size(min = 10, max = 500, message = "address must be 10 to 500 characters")
        String address,

        @NotBlank(message = "pincode is required")
        @Pattern(regexp = "\\d{6}", message = "pincode must be 6 digits")
        String pincode,

        @NotNull(message = "payment method is required")
        SareeOrder.PaymentMethod paymentMethod,

        // Required for UPI (checked in the controller). Null is fine for cash on delivery.
        @Pattern(regexp = "[A-Za-z0-9]{8,30}", message = "enter the transaction ID from your payment app")
        String transactionId
) {}