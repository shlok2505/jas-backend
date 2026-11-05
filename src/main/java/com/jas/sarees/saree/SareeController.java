package com.jas.sarees.saree;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sarees")
public class SareeController {

    private final SareeRepository repository;

    public SareeController(SareeRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<SareeResponse> listLive() {
        return repository
                .findByStatusOrderByCreatedAtDesc(Saree.Status.LIVE)
                .stream()
                .map(SareeResponse::from)
                .toList();
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SareeResponse create(@Valid @RequestBody SareeRequest request) {
        Saree saree = new Saree();
        saree.setType(request.type().trim());
        saree.setPrice(request.price());
        saree.setSupplierWhatsapp(request.supplierWhatsapp());
        return SareeResponse.from(repository.save(saree));
    }
}