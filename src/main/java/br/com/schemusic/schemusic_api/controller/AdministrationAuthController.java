package br.com.schemusic.schemusic_api.controller;

import br.com.schemusic.schemusic_api.bean.LoginRequestBean;
import br.com.schemusic.schemusic_api.delegate.AdministrationAuthDelegate;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/administration")
public class AdministrationAuthController {

    private final AdministrationAuthDelegate administrationAuthDelegate;

    public AdministrationAuthController(AdministrationAuthDelegate administrationAuthDelegate) {
        this.administrationAuthDelegate = administrationAuthDelegate;
    }

    @GetMapping
    public ResponseEntity<?> administrationHome() {
        return ResponseEntity.ok(Map.of(
                "mensagem", "Area administrativa do ScheMusic",
                "login", "/administration/login"
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestBean request) {
        return administrationAuthDelegate.login(request);
    }
}
