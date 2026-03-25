package br.com.schemusic.schemusic_api.controller;

import br.com.schemusic.schemusic_api.delegate.AdministrationSystemConfigDelegate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/administration/system-configs")
public class AdministrationSystemConfigController {

    private final AdministrationSystemConfigDelegate administrationSystemConfigDelegate;

    public AdministrationSystemConfigController(AdministrationSystemConfigDelegate administrationSystemConfigDelegate) {
        this.administrationSystemConfigDelegate = administrationSystemConfigDelegate;
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Map<String, Object> body) {
        return administrationSystemConfigDelegate.criar(body);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return administrationSystemConfigDelegate.editar(id, body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        return administrationSystemConfigDelegate.excluir(id);
    }

    @GetMapping
    public ResponseEntity<?> listar() {
        return administrationSystemConfigDelegate.listar();
    }
}
