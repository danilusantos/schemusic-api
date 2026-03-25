package br.com.schemusic.schemusic_api.delegate;

import br.com.schemusic.schemusic_api.business.AdminUsuarioBusiness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class AdministrationUserDelegate {

    private static final String FIELD_IDIOMA_PADRAO = "idiomaPadrao";

    private final AdminUsuarioBusiness adminUsuarioBusiness;

    public AdministrationUserDelegate(AdminUsuarioBusiness adminUsuarioBusiness) {
        this.adminUsuarioBusiness = adminUsuarioBusiness;
    }

    public ResponseEntity<?> criar(Map<String, Object> body) {
        try {
            String nome = (String) body.get("nome");
            String email = (String) body.get("email");
            String senha = (String) body.get("senha");
            String idiomaPadrao = (String) body.get(FIELD_IDIOMA_PADRAO);
            return ResponseEntity.status(HttpStatus.CREATED).body(adminUsuarioBusiness.criarAdmin(nome, email, senha, idiomaPadrao));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        }
    }

    public ResponseEntity<?> editar(Long id, Map<String, Object> body) {
        try {
            return ResponseEntity.ok(adminUsuarioBusiness.editar(
                    id,
                    (String) body.get("nome"),
                    (String) body.get("email"),
                    (String) body.get("senha"),
                        (String) body.get(FIELD_IDIOMA_PADRAO),
                    (Boolean) body.get("ativo")
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        }
    }

    public ResponseEntity<?> vincularRoles(Long id, Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<Object> payload = (List<Object>) body.get("roleIds");
            if (payload == null) {
                throw new IllegalArgumentException("Campo roleIds obrigatorio");
            }

            List<Long> roleIds = payload.stream()
                    .map(item -> {
                        if (item instanceof Number number) {
                            return number.longValue();
                        }
                        return Long.parseLong(String.valueOf(item));
                    })
                    .toList();
            return ResponseEntity.ok(adminUsuarioBusiness.vincularRoles(id, roleIds));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        }
    }

    public ResponseEntity<?> inativar(Long id) {
        try {
            adminUsuarioBusiness.inativar(id);
            return ResponseEntity.ok(Map.of("mensagem", "Usuario inativado com sucesso"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        }
    }

    public ResponseEntity<?> ativar(Long id) {
        try {
            adminUsuarioBusiness.ativar(id);
            return ResponseEntity.ok(Map.of("mensagem", "Usuario ativado com sucesso"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        }
    }

    public ResponseEntity<?> listar(Boolean ativo, String termo, Long roleId) {
        return ResponseEntity.ok(adminUsuarioBusiness.listar(ativo, termo, roleId));
    }

    public ResponseEntity<?> atualizarIdiomaPadrao(Long id, Map<String, Object> body) {
        try {
            String idiomaPadrao = (String) body.get(FIELD_IDIOMA_PADRAO);
            return ResponseEntity.ok(adminUsuarioBusiness.atualizarIdiomaPadrao(id, idiomaPadrao));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        }
    }
}
