package br.com.schemusic.schemusic_api.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.schemusic.schemusic_api.dto.EmpresaRequest;
import br.com.schemusic.schemusic_api.dto.EmpresaResponse;
import br.com.schemusic.schemusic_api.service.EmpresaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/empresas")
public class EmpresaController {

    private final EmpresaService service;

    public EmpresaController(EmpresaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EmpresaResponse> create(@Valid @RequestBody EmpresaRequest req) {
        EmpresaResponse resp = service.create(req);
        return ResponseEntity.status(201).body(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpresaResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<EmpresaResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return ResponseEntity.ok(service.list(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmpresaResponse> update(@PathVariable Long id, @Valid @RequestBody EmpresaRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
