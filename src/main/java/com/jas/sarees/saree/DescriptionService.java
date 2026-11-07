package com.jas.sarees.saree;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Service
public class DescriptionService {

    public record Description(String title, String text) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Reply(List<Candidate> candidates) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        record Candidate(Content content) {}

        @JsonIgnoreProperties(ignoreUnknown = true)
        record Content(List<Part> parts) {}

        @JsonIgnoreProperties(ignoreUnknown = true)
        record Part(String text) {}
    }

    private final RestClient client = RestClient.create();
    private final String apiKey;
    private final String endpoint;

    public DescriptionService(@Value("${gemini.api-key}") String apiKey,
                              @Value("${gemini.model}") String model) {
        this.apiKey = apiKey;
        this.endpoint = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent";
    }

    public boolean isConfigured() {
        return !apiKey.isBlank();
    }

    public Description describe(byte[] image, String mediaType, String sareeType) {
        String prompt = """
                You are writing a product listing for an online saree shop.
                The seller says this saree is: %s.
                Look at the photo and write exactly this:
                Line 1: a title of at most 8 words, with no quotes and no label.
                Then a blank line.
                Then a customer-facing description of 50 to 80 words covering the colours,
                border, motifs and pattern you can actually see, how it would drape,
                and two occasions it suits.
                Rules: treat the seller's description as the true fabric and never guess a
                different fabric or weave. Never mention price, brand, shipping or the shop.
                Always name the main colour with one simple, common colour word.
                Describe decoration by how it looks, for example "floral motifs with a
                sequin-like shimmer". Do not name a craft technique such as embroidery,
                zari or handloom unless the seller mentioned it.
                Plain text only, no markdown, no emojis.
                """.formatted(sareeType);

        Map<String, Object> body = Map.of(
        "contents", List.of(Map.of(
                "parts", List.of(
                        Map.of("inline_data", Map.of(
                                "mime_type", mediaType,
                                "data", Base64.getEncoder().encodeToString(image))),
                        Map.of("text", prompt)))),
        "generationConfig", Map.of("temperature", 0.4));

        Reply reply = client.post()
                .uri(endpoint)
                .header("x-goog-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Reply.class);

        if (reply == null || reply.candidates() == null || reply.candidates().isEmpty()
                || reply.candidates().get(0).content() == null) {
            throw new IllegalStateException("AI returned no text");
        }
        String text = reply.candidates().get(0).content().parts().stream()
                .map(Reply.Part::text)
                .filter(t -> t != null)
                .collect(Collectors.joining())
                .trim();
        if (text.isEmpty()) {
            throw new IllegalStateException("AI returned no text");
        }

        int newline = text.indexOf('\n');
        if (newline < 0) {
            return new Description(sareeType, text);
        }
        String title = text.substring(0, newline).replaceAll("^[#*\"\\s]+|[*\"\\s]+$", "");
        String description = text.substring(newline).trim();
        return new Description(title, description);
    }
}