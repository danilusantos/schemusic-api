package br.com.schemusic.schemusic_api.controller;

import br.com.schemusic.schemusic_api.bean.LoginRequestBean;
import br.com.schemusic.schemusic_api.delegate.PublicAuthDelegate;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import java.util.Map;

@RestController
@RequestMapping("/")
public class PublicAuthController {

    private final PublicAuthDelegate publicAuthDelegate;

    public PublicAuthController(PublicAuthDelegate publicAuthDelegate) {
        this.publicAuthDelegate = publicAuthDelegate;
    }

    @GetMapping
    public ResponseEntity<?> index() {
        return ResponseEntity.ok(Map.of(
                "mensagem", "API publica do ScheMusic",
                "login", "/login",
                "administracao", "/administration/login"
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestBean request) {
        return publicAuthDelegate.login(request);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        List<String> perfis = authentication.getAuthorities()
                .stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .toList();

        return ResponseEntity.ok(Map.of(
                "id", authentication.getName(),
                "perfis", perfis
        ));
    }
}
