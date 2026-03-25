package br.com.schemusic.schemusic_api.controller;

import br.com.schemusic.schemusic_api.delegate.AdministrationRoleDelegate;
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
@RequestMapping("/administration/roles")
public class AdministrationRoleController {

    private final AdministrationRoleDelegate administrationRoleDelegate;

    public AdministrationRoleController(AdministrationRoleDelegate administrationRoleDelegate) {
        this.administrationRoleDelegate = administrationRoleDelegate;
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Map<String, Object> body) {
        return administrationRoleDelegate.criar(body);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return administrationRoleDelegate.editar(id, body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        return administrationRoleDelegate.excluir(id);
    }

    @GetMapping
    public ResponseEntity<?> listar() {
        return administrationRoleDelegate.listar();
    }

    @GetMapping("/{id}/users")
    public ResponseEntity<?> listarUsuariosDaRole(@PathVariable Long id) {
        return administrationRoleDelegate.listarUsuariosDaRole(id);
    }
}
