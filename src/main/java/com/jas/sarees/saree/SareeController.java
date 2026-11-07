package com.jas.sarees.saree;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sarees")
public class SareeController {

    private final SareeRepository repository;
    private final ImageStorageService imageStorage;

    public SareeController(SareeRepository repository, ImageStorageService imageStorage) {
        this.repository = repository;
        this.imageStorage = imageStorage;
    }

    @GetMapping
    public List<SareeResponse> listLive() {
        return repository
                .findByStatusOrderByCreatedAtDesc(Saree.Status.LIVE)
                .stream()
                .map(SareeResponse::from)
                .toList();
    }
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SareeResponse create(@Valid @ModelAttribute SareeRequest request) throws IOException {
        Saree saree = new Saree();
        saree.setType(request.type().trim());
        saree.setPrice(request.price());
        saree.setSupplierWhatsapp(request.supplierWhatsapp());
        saree.setImageUrl(imageStorage.store(request.photo()));
        return SareeResponse.from(repository.save(saree));
    }
}