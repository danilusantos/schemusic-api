package br.com.schemusic.schemusic_api.delegate;

import br.com.schemusic.schemusic_api.business.AdminListaAcessoBusiness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AdministrationAccessListDelegate {

    private final AdminListaAcessoBusiness adminListaAcessoBusiness;

    public AdministrationAccessListDelegate(AdminListaAcessoBusiness adminListaAcessoBusiness) {
        this.adminListaAcessoBusiness = adminListaAcessoBusiness;
    }

    public ResponseEntity<?> criar(Map<String, Object> body) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(adminListaAcessoBusiness.criar(
                    (String) body.get("tipoLista"),
                    (String) body.get("tipoAlvo"),
                    (String) body.get("valorAlvo"),
                    (String) body.get("observacao"),
                    (Boolean) body.get("ativo")
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        }
    }

    public ResponseEntity<?> editar(Long id, Map<String, Object> body) {
        try {
            return ResponseEntity.ok(adminListaAcessoBusiness.editar(
                    id,
                    (String) body.get("tipoLista"),
                    (String) body.get("tipoAlvo"),
                    (String) body.get("valorAlvo"),
                    (String) body.get("observacao"),
                    (Boolean) body.get("ativo")
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        }
    }

    public ResponseEntity<?> inativar(Long id) {
        adminListaAcessoBusiness.inativar(id);
        return ResponseEntity.ok(Map.of("mensagem", "Item inativado com sucesso"));
    }

    public ResponseEntity<?> listar(String tipoLista, Boolean ativo) {
        return ResponseEntity.ok(adminListaAcessoBusiness.listar(tipoLista, ativo));
    }
}
