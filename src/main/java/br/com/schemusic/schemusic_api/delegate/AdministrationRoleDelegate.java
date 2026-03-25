package br.com.schemusic.schemusic_api.delegate;

import br.com.schemusic.schemusic_api.business.AdminRoleBusiness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AdministrationRoleDelegate {

    private final AdminRoleBusiness adminRoleBusiness;

    public AdministrationRoleDelegate(AdminRoleBusiness adminRoleBusiness) {
        this.adminRoleBusiness = adminRoleBusiness;
    }

    public ResponseEntity<?> criar(Map<String, Object> body) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(adminRoleBusiness.criar(
                    (String) body.get("nome"),
                    (String) body.get("descricao"),
                    (Boolean) body.get("ativo")
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        }
    }

    public ResponseEntity<?> editar(Long id, Map<String, Object> body) {
        try {
            return ResponseEntity.ok(adminRoleBusiness.editar(
                    id,
                    (String) body.get("nome"),
                    (String) body.get("descricao"),
                    (Boolean) body.get("ativo")
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        }
    }

    public ResponseEntity<?> excluir(Long id) {
        try {
            adminRoleBusiness.excluir(id);
            return ResponseEntity.ok(Map.of("mensagem", "Role excluida com sucesso"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        }
    }

    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(adminRoleBusiness.listar());
    }

    public ResponseEntity<?> listarUsuariosDaRole(Long idRole) {
        try {
            return ResponseEntity.ok(adminRoleBusiness.listarUsuariosDaRole(idRole));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        }
    }
}
