package br.com.schemusic.schemusic_api.controller;

import br.com.schemusic.schemusic_api.util.DatabaseQueryUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/debug/queries")
public class DatabaseDebugController {

    @Autowired
    private DatabaseQueryUtil queryUtil;

    @GetMapping("/telas")
    public ResponseEntity<?> listarTodasAsTelas() {
        try {
            List<Map<String, Object>> telas = queryUtil.executarQuery("SELECT DISTINCT tela_codigo FROM PERMISSAO_TELA ORDER BY tela_codigo");
            return ResponseEntity.ok(Map.of("telas", telas, "total", telas.size()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/grupos")
    public ResponseEntity<?> listarTodosOsGrupos() {
        try {
            List<Map<String, Object>> grupos = queryUtil.executarQuery("SELECT id_grupo, codigo_grupo, nome_grupo FROM PERMISSAO_GRUPO ORDER BY nome_grupo");
            return ResponseEntity.ok(Map.of("grupos", grupos, "total", grupos.size()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/telas-admin-orphans")
    public ResponseEntity<?> listarTelasAdminOrfas() {
        try {
            // Telas mapeadas no código
            Map<String, String> telasValidas = Map.ofEntries(
                    Map.entry("ADMIN_DASHBOARD", "Dashboard administrativo"),
                    Map.entry("ADMIN_USERS", "Usuarios administrativos"),
                    Map.entry("ADMIN_ROLES", "Perfis de acesso"),
                    Map.entry("ADMIN_ACCESS_CONTROL", "Controle de permissoes"),
                    Map.entry("ADMIN_CONFIGS", "Configuracoes do sistema"),
                    Map.entry("ADMIN_ACCESS_LISTS", "Listas de acesso"),
                    Map.entry("ADMIN_ACCESS_LOGS", "Logs de acesso")
            );

            // Buscar todas as telas que começam com ADMIN_
            List<Map<String, Object>> telasBD = queryUtil.executarQuery(
                    "SELECT DISTINCT pt.tela_codigo, pg.codigo_grupo, pg.nome_grupo " +
                    "FROM PERMISSAO_TELA pt " +
                    "LEFT JOIN PERMISSAO_GRUPO pg ON pt.id_grupo = pg.id_grupo " +
                    "WHERE pt.tela_codigo LIKE 'ADMIN_%' " +
                    "ORDER BY pt.tela_codigo"
            );

            // Separar em válidas e órfãs
            List<Map<String, Object>> telasValidas_BD = new java.util.ArrayList<>();
            List<Map<String, Object>> telasOrfas = new java.util.ArrayList<>();

            for (Map<String, Object> tela : telasBD) {
                String telaCodigo = (String) tela.get("tela_codigo");
                if (telasValidas.containsKey(telaCodigo)) {
                    telasValidas_BD.add(tela);
                } else {
                    telasOrfas.add(tela);
                }
            }

            Map<String, Object> resposta = new HashMap<>();
            resposta.put("telasValidas", telasValidas_BD);
            resposta.put("telasValidas_total", telasValidas_BD.size());
            resposta.put("telasOrfas", telasOrfas);
            resposta.put("telasOrfas_total", telasOrfas.size());
            resposta.put("telasEsperadasNoCodigo", telasValidas.keySet());
            resposta.put("telasEsperadas_total", telasValidas.size());

            return ResponseEntity.ok(resposta);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/todas-as-queries")
    public ResponseEntity<?> executarTodasAsQueries() {
        try {
            List<Map<String, Object>> telas = queryUtil.executarQuery("SELECT DISTINCT tela_codigo FROM PERMISSAO_TELA ORDER BY tela_codigo");
            List<Map<String, Object>> grupos = queryUtil.executarQuery("SELECT id_grupo, codigo_grupo, nome_grupo FROM PERMISSAO_GRUPO ORDER BY nome_grupo");
            
            // Telas mapeadas no código
            Map<String, String> telasValidas = Map.ofEntries(
                    Map.entry("ADMIN_DASHBOARD", "Dashboard administrativo"),
                    Map.entry("ADMIN_USERS", "Usuarios administrativos"),
                    Map.entry("ADMIN_ROLES", "Perfis de acesso"),
                    Map.entry("ADMIN_ACCESS_CONTROL", "Controle de permissoes"),
                    Map.entry("ADMIN_CONFIGS", "Configuracoes do sistema"),
                    Map.entry("ADMIN_ACCESS_LISTS", "Listas de acesso"),
                    Map.entry("ADMIN_ACCESS_LOGS", "Logs de acesso")
            );

            // Buscar detalhes de telas ADMIN_
            List<Map<String, Object>> telasBD = queryUtil.executarQuery(
                    "SELECT DISTINCT pt.tela_codigo, pg.codigo_grupo, pg.nome_grupo, COUNT(pt.id_permissao) as total_permissoes " +
                    "FROM PERMISSAO_TELA pt " +
                    "LEFT JOIN PERMISSAO_GRUPO pg ON pt.id_grupo = pg.id_grupo " +
                    "WHERE pt.tela_codigo LIKE 'ADMIN_%' " +
                    "GROUP BY pt.tela_codigo, pg.codigo_grupo, pg.nome_grupo " +
                    "ORDER BY pt.tela_codigo"
            );

            List<Map<String, Object>> telasOrfas = new java.util.ArrayList<>();
            for (Map<String, Object> tela : telasBD) {
                String telaCodigo = (String) tela.get("tela_codigo");
                if (!telasValidas.containsKey(telaCodigo)) {
                    telasOrfas.add(tela);
                }
            }

            Map<String, Object> resposta = new HashMap<>();
            resposta.put("query_1_telas_distintas", Map.of(
                    "descricao", "Todas as telas distintas na tabela PERMISSAO_TELA",
                    "total", telas.size(),
                    "dados", telas
            ));
            resposta.put("query_2_grupos", Map.of(
                    "descricao", "Todos os grupos na tabela PERMISSAO_GRUPO",
                    "total", grupos.size(),
                    "dados", grupos
            ));
            resposta.put("query_3_telas_admin", Map.of(
                    "descricao", "Telas ADMIN_ com detalhes",
                    "total", telasBD.size(),
                    "dados", telasBD
            ));
            resposta.put("query_4_telas_orfa", Map.of(
                    "descricao", "Telas ADMIN_ que NÃO estão no mapa do código",
                    "total", telasOrfas.size(),
                    "mapa_esperado", telasValidas,
                    "dados", telasOrfas
            ));

            return ResponseEntity.ok(resposta);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage(), "stack", e.getStackTrace()));
        }
    }
}
