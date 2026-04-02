package br.com.schemusic.schemusic_api.delegate;

import br.com.schemusic.schemusic_api.business.AdminAccessControlBusiness;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AdministrationAccessControlDelegate {

    private final AdminAccessControlBusiness adminAccessControlBusiness;

    public AdministrationAccessControlDelegate(AdminAccessControlBusiness adminAccessControlBusiness) {
        this.adminAccessControlBusiness = adminAccessControlBusiness;
    }

    public ResponseEntity<?> listarCatalogo() {
        return ResponseEntity.ok(adminAccessControlBusiness.listarCatalogo());
    }

    public ResponseEntity<?> listarGruposCatalogo() {
        return ResponseEntity.ok(adminAccessControlBusiness.listarGruposCatalogo());
    }

    public ResponseEntity<?> criarGrupoCatalogo(Map<String, Object> body) {
        try {
            String codigoGrupo = String.valueOf(body.get("codigoGrupo"));
            String nomeGrupo = String.valueOf(body.get("nomeGrupo"));
            String descricao = body.get("descricao") == null ? null : String.valueOf(body.get("descricao"));
            return ResponseEntity.ok(adminAccessControlBusiness.criarGrupoCatalogo(codigoGrupo, nomeGrupo, descricao));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        }
    }

    public ResponseEntity<?> criarTelaCatalogo(Map<String, Object> body) {
        try {
            String codigoGrupo = String.valueOf(body.get("codigoGrupo"));
            String telaCodigo = String.valueOf(body.get("telaCodigo"));
            String telaNome = String.valueOf(body.get("telaNome"));
            String descricao = body.get("descricao") == null ? null : String.valueOf(body.get("descricao"));
            return ResponseEntity.ok(adminAccessControlBusiness.criarTelaCatalogo(codigoGrupo, telaCodigo, telaNome, descricao));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        }
    }

    public ResponseEntity<?> deletarGrupoCatalogo(String codigoGrupo) {
        try {
            adminAccessControlBusiness.deletarGrupoCatalogo(codigoGrupo);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        }
    }

    public ResponseEntity<?> deletarTelaCatalogo(String codigoTela) {
        try {
            adminAccessControlBusiness.deletarTelaCatalogo(codigoTela);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        }
    }

    public ResponseEntity<?> obterPermissoesDaRole(Long idRole) {
        try {
            return ResponseEntity.ok(adminAccessControlBusiness.obterPermissoesDaRole(idRole));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        }
    }

    public ResponseEntity<?> atualizarPermissoesDaRole(Long idRole, Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<Object> payload = (List<Object>) body.get("idsPermissao");
            if (payload == null) {
                throw new IllegalArgumentException("Campo idsPermissao obrigatorio");
            }

            List<Long> idsPermissao = payload.stream()
                    .map(item -> item instanceof Number number ? number.longValue() : Long.parseLong(String.valueOf(item)))
                    .toList();

            return ResponseEntity.ok(adminAccessControlBusiness.atualizarPermissoesDaRole(idRole, idsPermissao));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        }
    }

    public ResponseEntity<?> obterPermissoesDoUsuario(Long idUsuario) {
        try {
            return ResponseEntity.ok(adminAccessControlBusiness.obterPermissoesDoUsuario(idUsuario));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        }
    }

    public ResponseEntity<?> atualizarPermissoesDoUsuario(Long idUsuario, Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> payload = (List<Map<String, Object>>) body.get("overrides");
            if (payload == null) {
                payload = new ArrayList<>();
            }
            return ResponseEntity.ok(adminAccessControlBusiness.atualizarPermissoesDoUsuario(idUsuario, payload));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        }
    }

    public ResponseEntity<?> consultarPermissoesDiretasUsuario(Long idUsuario) {
        try {
            return ResponseEntity.ok(adminAccessControlBusiness.consultarPermissoesDiretasPorId(idUsuario));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        }
    }

    public ResponseEntity<?> consultarPermissoesDiretasUsuarioPorEmail(String email) {
        try {
            return ResponseEntity.ok(adminAccessControlBusiness.consultarPermissoesDiretasPorEmail(email));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        }
    }

    public ResponseEntity<?> obterAcessoDaTelaAtual(Long idUsuario, String telaCodigo, String rota) {
        try {
            String telaResolvida = telaCodigo;
            if ((telaResolvida == null || telaResolvida.isBlank()) && rota != null && !rota.isBlank()) {
                telaResolvida = adminAccessControlBusiness.resolverTelaPorRota(rota);
            }
            return ResponseEntity.ok(adminAccessControlBusiness.obterAcessoEfetivoDoUsuario(idUsuario, telaResolvida));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        }
    }

    public ResponseEntity<?> excluirGruposCatalogo(Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<Object> payload = (List<Object>) body.get("ids");
            if (payload == null) {
                throw new IllegalArgumentException("Campo ids obrigatorio");
            }

            List<Long> idsGrupo = payload.stream()
                    .map(item -> item instanceof Number number ? number.longValue() : Long.parseLong(String.valueOf(item)))
                    .toList();

            return ResponseEntity.ok(adminAccessControlBusiness.excluirGruposCatalogo(idsGrupo));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        }
    }

    public ResponseEntity<?> excluirTelasCatalogo(Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<Object> payload = (List<Object>) body.get("ids");
            if (payload == null) {
                throw new IllegalArgumentException("Campo ids obrigatorio");
            }

            List<Long> idsPermissao = payload.stream()
                    .map(item -> item instanceof Number number ? number.longValue() : Long.parseLong(String.valueOf(item)))
                    .toList();

            return ResponseEntity.ok(adminAccessControlBusiness.excluirTelasCatalogo(idsPermissao));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
        }
    }
}
