package br.com.schemusic.schemusic_api.delegate;

import br.com.schemusic.schemusic_api.bean.LoginRequestBean;
import br.com.schemusic.schemusic_api.bean.LoginResponseBean;
import br.com.schemusic.schemusic_api.business.AuthBusiness;
import br.com.schemusic.schemusic_api.exception.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PublicAuthDelegate {

    private final AuthBusiness authBusiness;

    public PublicAuthDelegate(AuthBusiness authBusiness) {
        this.authBusiness = authBusiness;
    }

    public ResponseEntity<?> login(LoginRequestBean request) {
        try {
            LoginResponseBean response = authBusiness.loginArtista(request.email(), request.senha());
            return ResponseEntity.ok(response);
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", ex.getMessage()));
        }
    }
}
