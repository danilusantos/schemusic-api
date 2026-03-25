package br.com.schemusic.schemusic_api.controller;

import br.com.schemusic.schemusic_api.delegate.AdministrationAccessListDelegate;
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
@RequestMapping("/administration/access-lists")
public class AdministrationAccessListController {

    private final AdministrationAccessListDelegate administrationAccessListDelegate;

    public AdministrationAccessListController(AdministrationAccessListDelegate administrationAccessListDelegate) {
        this.administrationAccessListDelegate = administrationAccessListDelegate;
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Map<String, Object> body) {
        return administrationAccessListDelegate.criar(body);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return administrationAccessListDelegate.editar(id, body);
    }

    @PutMapping("/{id}/inativar")
    public ResponseEntity<?> inativar(@PathVariable Long id) {
        return administrationAccessListDelegate.inativar(id);
    }

    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(required = false) String tipoLista,
            @RequestParam(required = false) Boolean ativo
    ) {
        return administrationAccessListDelegate.listar(tipoLista, ativo);
    }
}
