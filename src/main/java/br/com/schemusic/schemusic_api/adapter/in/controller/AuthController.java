package br.com.schemusic.schemusic_api.adapter.in.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.schemusic.schemusic_api.config.security.JwtProvider;
import br.com.schemusic.schemusic_api.dto.request.LoginRequest;
import br.com.schemusic.schemusic_api.dto.response.AuthResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtProvider jwtProvider;

    public AuthController(AuthenticationManager authManager, JwtProvider jwtProvider) {
        this.authManager = authManager;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );
        String username = auth.getName();
        String roles = auth.getAuthorities().toString();
        String access = jwtProvider.generateAccessToken(username, roles);
        String refresh = jwtProvider.generateRefreshToken(username);
        return ResponseEntity.ok(new AuthResponse(access, refresh));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody String refreshToken) {
        String token = refreshToken.replace("\"", "").trim();
        if (!jwtProvider.validateToken(token)) {
            return ResponseEntity.status(401).body("Refresh token inválido");
        }
        String username = jwtProvider.getUsernameFromToken(token);
        String newAccess = jwtProvider.generateAccessToken(username, "ROLE_ADMIN");
        return ResponseEntity.ok(new AuthResponse(newAccess, token));
    }
}
