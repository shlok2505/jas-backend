package com.jas.sarees.saree;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// Owner-only listing management. Creating a saree stays on POST /api/sarees.
// Sarees are never deleted because past orders point at them: hide them instead.
@RestController
@RequestMapping("/api/admin/sarees")
public class AdminSareeController {

    private final SareeRepository repository;

    public AdminSareeController(SareeRepository repository) {
        this.repository = repository;
    }

    // Every field is optional: only the ones sent are changed.
    public record UpdateRequest(
            @Size(max = 100, message = "type is too long")
            String type,

            @Positive(message = "price must be more than 0")
            BigDecimal price,

            @Size(max = 200, message = "title is too long")
            String title,

            @Size(max = 5000, message = "description is too long")
            String description,

            @Pattern(regexp = "\\d{10,15}", message = "use digits only, with country code, e.g. 919876543210")
            String supplierWhatsapp,

            Saree.Status status
    ) {}

    @GetMapping
    public List<AdminSareeResponse> listAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .map(AdminSareeResponse::from)
                .toList();
    }

    @PatchMapping("/{id}")
    public AdminSareeResponse update(@PathVariable Long id, @Valid @RequestBody UpdateRequest request) {
        Saree saree = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Saree not found"));

        if (request.type() != null && !request.type().isBlank()) saree.setType(request.type().trim());
        if (request.price() != null) saree.setPrice(request.price());
        if (request.title() != null) saree.setTitle(request.title().trim());
        if (request.description() != null) saree.setDescription(request.description().trim());
        if (request.supplierWhatsapp() != null) saree.setSupplierWhatsapp(request.supplierWhatsapp());
        if (request.status() != null) saree.setStatus(request.status());

        return AdminSareeResponse.from(repository.save(saree));
    }
}
