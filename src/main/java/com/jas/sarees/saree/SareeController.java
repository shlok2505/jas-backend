package com.jas.sarees.saree;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sarees")
public class SareeController {
    private static final Logger log = LoggerFactory.getLogger(SareeController.class);

    private final SareeRepository repository;
    private final ImageStorageService imageStorage;
    private final DescriptionService descriptionService;

    public SareeController(SareeRepository repository, ImageStorageService imageStorage,DescriptionService descriptionService) {
        this.repository = repository;
        this.imageStorage = imageStorage;
        this.descriptionService = descriptionService;
    }

    @GetMapping
    public List<SareeResponse> listLive() {
        return repository
                .findByStatusOrderByCreatedAtDesc(Saree.Status.LIVE)
                .stream()
                .map(SareeResponse::from)
                .toList();
    }
    @GetMapping("/{id}")
    public SareeResponse get(@PathVariable Long id) {
        return repository.findById(id)
                .filter(s -> s.getStatus() == Saree.Status.LIVE)
                .map(SareeResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Saree not found"));
    }
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SareeResponse create(@Valid @ModelAttribute SareeRequest request) throws IOException {
        Saree saree = new Saree();
        saree.setType(request.type().trim());
        saree.setPrice(request.price());
        saree.setSupplierWhatsapp(request.supplierWhatsapp());
        saree.setImageUrl(imageStorage.store(request.photo()));
        applyDescription(saree, request.photo());
        return SareeResponse.from(repository.save(saree));
    }
        private void applyDescription(Saree saree, MultipartFile photo) {
        String type = saree.getType();
        saree.setTitle(type + " Saree");
        saree.setDescription("A beautiful " + type.toLowerCase() + " saree from our latest collection.");

        if (!descriptionService.isConfigured()) {
            return;
        }
        try {
            DescriptionService.Description d =
                    descriptionService.describe(photo.getBytes(), photo.getContentType(), type);
            String title = d.title();
            saree.setTitle(title.length() > 200 ? title.substring(0, 200) : title);
            saree.setDescription(d.text());
        } catch (Exception e) {
            log.warn("AI description failed, keeping the template: {}", e.getMessage());
        }
    }
}