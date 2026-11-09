package com.jas.sarees.order;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SupplierMessage {

    private final String baseUrl;

    public SupplierMessage(@Value("${app.public-base-url}") String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public String link(SareeOrder o) {
        String text = """
                New order #%d
                Saree: %s
                Price: Rs %s
                Photo: %s%s

                Deliver to:
                %s
                %s
                Pincode: %s
                """.formatted(
                o.getId(),
                o.getSaree().getTitle(),
                o.getPriceAtOrder().toPlainString(),
                baseUrl, o.getSaree().getImageUrl(),
                o.getCustomerName(),
                o.getAddress(),
                o.getPincode());

        String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8).replace("+", "%20");
        return "https://wa.me/" + o.getSaree().getSupplierWhatsapp() + "?text=" + encoded;
    }
}