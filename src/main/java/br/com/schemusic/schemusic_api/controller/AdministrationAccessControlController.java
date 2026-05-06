package br.com.schemusic.schemusic_api.controller;

import br.com.schemusic.schemusic_api.delegate.AdministrationAccessControlDelegate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/administration/access-control")
public class AdministrationAccessControlController {

    private final AdministrationAccessControlDelegate administrationAccessControlDelegate;

    public AdministrationAccessControlController(AdministrationAccessControlDelegate administrationAccessControlDelegate) {
        this.administrationAccessControlDelegate = administrationAccessControlDelegate;
    }

    @GetMapping("/roles/{idRole}")
    public ResponseEntity<?> obterPermissoesDaRole(@PathVariable Long idRole) {
        return administrationAccessControlDelegate.obterPermissoesDaRole(idRole);
    }

    @PutMapping("/roles/{idRole}")
    public ResponseEntity<?> atualizarPermissoesDaRole(@PathVariable Long idRole, @RequestBody Map<String, Object> body) {
        return administrationAccessControlDelegate.atualizarPermissoesDaRole(idRole, body);
    }

    @GetMapping("/users/{idUsuario}")
    public ResponseEntity<?> obterPermissoesDoUsuario(@PathVariable Long idUsuario) {
        return administrationAccessControlDelegate.obterPermissoesDoUsuario(idUsuario);
    }

    @GetMapping("/users/{idUsuario}/direct")
    public ResponseEntity<?> consultarPermissoesDiretasDoUsuario(@PathVariable Long idUsuario) {
        return administrationAccessControlDelegate.consultarPermissoesDiretasUsuario(idUsuario);
    }

    @GetMapping("/users/direct")
    public ResponseEntity<?> consultarPermissoesDiretasDoUsuarioPorEmail(@RequestParam String email) {
        return administrationAccessControlDelegate.consultarPermissoesDiretasUsuarioPorEmail(email);
    }

    @PutMapping("/users/{idUsuario}")
    public ResponseEntity<?> atualizarPermissoesDoUsuario(@PathVariable Long idUsuario, @RequestBody Map<String, Object> body) {
        return administrationAccessControlDelegate.atualizarPermissoesDoUsuario(idUsuario, body);
    }

    @GetMapping("/catalog")
    public ResponseEntity<?> listarCatalogoAcessos() {
        return administrationAccessControlDelegate.listarCatalogoAcessos();
    }

    @PostMapping("/catalog/screens/delete")
    public ResponseEntity<?> excluirTelasCatalogo(@RequestBody Map<String, Object> body) {
        return administrationAccessControlDelegate.excluirTelasCatalogo(body);
    }

    @GetMapping("/me")
    public ResponseEntity<?> meuAcesso(
            Authentication authentication,
            @RequestParam(required = false) String telaCodigo,
            @RequestParam(required = false) String rota
    ) {
        Long idUsuario = Long.parseLong(authentication.getName());
        return administrationAccessControlDelegate.obterAcessoDaTelaAtual(idUsuario, telaCodigo, rota);
    }
}
