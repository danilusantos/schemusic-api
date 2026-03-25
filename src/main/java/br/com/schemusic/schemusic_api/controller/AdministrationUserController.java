package br.com.schemusic.schemusic_api.controller;

import br.com.schemusic.schemusic_api.delegate.AdministrationUserDelegate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/administration/users")
public class AdministrationUserController {

    private final AdministrationUserDelegate administrationUserDelegate;

    public AdministrationUserController(AdministrationUserDelegate administrationUserDelegate) {
        this.administrationUserDelegate = administrationUserDelegate;
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Map<String, Object> body) {
        return administrationUserDelegate.criar(body);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return administrationUserDelegate.editar(id, body);
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<?> vincularRoles(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return administrationUserDelegate.vincularRoles(id, body);
    }

    @PutMapping("/{id}/inativar")
    public ResponseEntity<?> inativar(@PathVariable Long id) {
        return administrationUserDelegate.inativar(id);
    }

    @PutMapping("/{id}/ativar")
    public ResponseEntity<?> ativar(@PathVariable Long id) {
        return administrationUserDelegate.ativar(id);
    }

    @PutMapping("/{id}/idioma-padrao")
    public ResponseEntity<?> atualizarIdiomaPadrao(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return administrationUserDelegate.atualizarIdiomaPadrao(id, body);
    }

    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) String termo,
            @RequestParam(required = false) Long roleId
    ) {
        return administrationUserDelegate.listar(ativo, termo, roleId);
    }
}
