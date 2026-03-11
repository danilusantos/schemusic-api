package br.com.schemusic.schemusic_api.adapter.in.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.schemusic.schemusic_api.application.service.MusicoService;
import br.com.schemusic.schemusic_api.dto.request.MusicoRequest;
import br.com.schemusic.schemusic_api.dto.response.MusicoResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin/musicos")
@PreAuthorize("hasRole('ADMIN')")
public class MusicoController {

    private final MusicoService service;

    public MusicoController(MusicoService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<MusicoResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<MusicoResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return ResponseEntity.ok(service.list(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MusicoResponse> update(@PathVariable Long id, @Valid @RequestBody MusicoRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }
}
