package br.com.schemusic.schemusic_api.controller;

import br.com.schemusic.schemusic_api.delegate.AdministrationAccessLogDelegate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/administration/access-logs")
public class AdministrationAccessLogController {

    private final AdministrationAccessLogDelegate administrationAccessLogDelegate;

    public AdministrationAccessLogController(AdministrationAccessLogDelegate administrationAccessLogDelegate) {
        this.administrationAccessLogDelegate = administrationAccessLogDelegate;
    }

    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(required = false) Integer limite) {
        return administrationAccessLogDelegate.listar(limite);
    }

    @GetMapping("/series")
    public ResponseEntity<?> serie(@RequestParam(required = false, defaultValue = "week") String periodo) {
        return administrationAccessLogDelegate.serie(periodo);
    }

    @PostMapping("/frontend-navigation")
    public ResponseEntity<?> registrarNavegacaoFrontend(
            @RequestBody Map<String, Object> body,
            Authentication authentication,
            HttpServletRequest request
    ) {
        try {
            if (body == null) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Body nao pode ser vazio"));
            }
            
            String rota = (String) body.get("rota");
            String userAgent = (String) body.get("userAgent");
            String origem = (String) body.get("origem");
            String userId = authentication != null ? authentication.getName() : null;

            if (rota == null || rota.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Rota é obrigatoria"));
            }

            administrationAccessLogDelegate.registrarNavegacaoFrontend(
                    rota,
                    origem,
                    userAgent,
                    request.getRemoteAddr(),
                    userId
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("mensagem", "Navegacao registrada"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao registrar navegacao: " + ex.getMessage()));
        }
    }
}
