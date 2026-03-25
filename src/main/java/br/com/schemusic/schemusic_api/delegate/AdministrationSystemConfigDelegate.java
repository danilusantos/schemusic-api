package br.com.schemusic.schemusic_api.delegate;

import br.com.schemusic.schemusic_api.business.AdminSistemaConfigBusiness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AdministrationSystemConfigDelegate {

    private final AdminSistemaConfigBusiness adminSistemaConfigBusiness;

    public AdministrationSystemConfigDelegate(AdminSistemaConfigBusiness adminSistemaConfigBusiness) {
        this.adminSistemaConfigBusiness = adminSistemaConfigBusiness;
    }

    public ResponseEntity<?> criar(Map<String, Object> body) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(adminSistemaConfigBusiness.criar(
                    (String) body.get("chave"),
                    (String) body.get("valor"),
                    (String) body.get("descricao"),
                    (Boolean) body.get("ativo")
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        }
    }

    public ResponseEntity<?> editar(Long id, Map<String, Object> body) {
        try {
            return ResponseEntity.ok(adminSistemaConfigBusiness.editar(
                    id,
                    (String) body.get("chave"),
                    (String) body.get("valor"),
                    (String) body.get("descricao"),
                    (Boolean) body.get("ativo")
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        }
    }

    public ResponseEntity<?> excluir(Long id) {
        adminSistemaConfigBusiness.excluir(id);
        return ResponseEntity.ok(Map.of("mensagem", "Configuracao excluida com sucesso"));
    }

    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(adminSistemaConfigBusiness.listar());
    }
}
