package br.com.schemusic.schemusic_api.business;

import br.com.schemusic.schemusic_api.bean.PermissaoTelaBean;
import br.com.schemusic.schemusic_api.bean.PermissaoGrupoBean;
import br.com.schemusic.schemusic_api.bean.RoleBean;
import br.com.schemusic.schemusic_api.bean.UsuarioBean;
import br.com.schemusic.schemusic_api.dao.PermissaoGrupoDAO;
import br.com.schemusic.schemusic_api.dao.PermissaoTelaDAO;
import br.com.schemusic.schemusic_api.dao.RoleDAO;
import br.com.schemusic.schemusic_api.dao.RolePermissaoDAO;
import br.com.schemusic.schemusic_api.dao.UsuarioDAO;
import br.com.schemusic.schemusic_api.dao.UsuarioPermissaoDAO;
import br.com.schemusic.schemusic_api.dao.UsuarioRoleDAO;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class AdminAccessControlBusiness {

    private static final List<String> ACOES_PADRAO = List.of("CONSULTAR", "INCLUIR", "EDITAR", "EXCLUIR");
    private static final String GRUPO_PADRAO_CODIGO = "ADMIN";
    private static final String GRUPO_PADRAO_NOME = "Administracao";

    // JSON Key constants
    private static final String KEY_GRUPO_CODIGO = "grupoCodigo";
    private static final String KEY_GRUPO_NOME = "grupoNome";
    private static final String KEY_TELA_CODIGO = "telaCodigo";
    private static final String KEY_TELA_NOME = "telaNome";
    private static final String KEY_DESCRICAO = "descricao";
    private static final String KEY_GRUPOS = "grupos";
    private static final String KEY_TELAS = "telas";
    private static final String KEY_PERMISSOES = "permissoes";
    private static final String KEY_ID_PERMISSAO = "idPermissao";
    private static final String KEY_ID_GRUPO = "idGrupo";
    private static final String KEY_ACAO_CODIGO = "acaoCodigo";
    private static final String KEY_ATIVO = "ativo";

        private static final Map<String, String> TELAS_ADMIN = Map.ofEntries(
            Map.entry("ADMIN_DASHBOARD", "Dashboard administrativo"),
            Map.entry("ADMIN_USERS", "Usuários administrativos"),
            Map.entry("ADMIN_ROLES", "Perfis de acesso"),
            Map.entry("ADMIN_ACCESS_CONTROL", "Controle de permissões"),
            Map.entry("ADMIN_CONFIGS", "Configurações do sistema"),
            Map.entry("ADMIN_ACCESS_LISTS", "Listas de acesso"),
            Map.entry("ADMIN_ACCESS_LOGS", "Logs de acesso"),
            Map.entry("ADMIN_ACCESS_CATALOG", "Catálogo de permissões")
        );

    private final PermissaoGrupoDAO permissaoGrupoDAO;
    private final PermissaoTelaDAO permissaoTelaDAO;
    private final RolePermissaoDAO rolePermissaoDAO;
    private final UsuarioPermissaoDAO usuarioPermissaoDAO;
    private final UsuarioDAO usuarioDAO;
    private final RoleDAO roleDAO;
    private final UsuarioRoleDAO usuarioRoleDAO;
    private final DataSource dataSource;

    public AdminAccessControlBusiness(PermissaoGrupoDAO permissaoGrupoDAO,
                                      PermissaoTelaDAO permissaoTelaDAO,
                                      RolePermissaoDAO rolePermissaoDAO,
                                      UsuarioPermissaoDAO usuarioPermissaoDAO,
                                      UsuarioDAO usuarioDAO,
                                      RoleDAO roleDAO,
                                      UsuarioRoleDAO usuarioRoleDAO,
                                      DataSource dataSource) {
        this.permissaoGrupoDAO = permissaoGrupoDAO;
        this.permissaoTelaDAO = permissaoTelaDAO;
        this.rolePermissaoDAO = rolePermissaoDAO;
        this.usuarioPermissaoDAO = usuarioPermissaoDAO;
        this.usuarioDAO = usuarioDAO;
        this.roleDAO = roleDAO;
        this.usuarioRoleDAO = usuarioRoleDAO;
        this.dataSource = dataSource;
    }

    public void criarCatalogoPadraoSeNecessario() {
        PermissaoGrupoBean grupoPadrao = garantirGrupoPadrao();

        for (Map.Entry<String, String> tela : TELAS_ADMIN.entrySet()) {
            for (String acao : ACOES_PADRAO) {
                PermissaoTelaBean existente = permissaoTelaDAO.buscarPorTelaEAcao(tela.getKey(), acao);
                if (existente != null) {
                    if (!grupoPadrao.getIdGrupo().equals(existente.getIdGrupo())) {
                        permissaoTelaDAO.atualizarGrupoDaPermissao(existente.getIdPermissao(), grupoPadrao.getIdGrupo());
                    }
                    continue;
                }
                PermissaoTelaBean permissao = new PermissaoTelaBean();
                permissao.setIdGrupo(grupoPadrao.getIdGrupo());
                permissao.setTelaCodigo(tela.getKey());
                permissao.setAcaoCodigo(acao);
                permissao.setDescricao(tela.getValue() + " - " + acao);
                permissao.setAtivo(true);
                permissaoTelaDAO.inserir(permissao);
            }
        }
    }



    public Map<String, Object> obterPermissoesDaRole(Long idRole) {
        RoleBean role = roleDAO.buscarPorId(idRole);
        if (role == null) {
            throw new IllegalArgumentException("Role nao encontrada");
        }

        Set<Long> idsPermissao = new LinkedHashSet<>(rolePermissaoDAO.listarIdsPermissaoDaRole(idRole));
        return montarRespostaVinculos("role", idRole, idsPermissao, Map.of());
    }

    public Map<String, Object> atualizarPermissoesDaRole(Long idRole, List<Long> idsPermissao) {
        RoleBean role = roleDAO.buscarPorId(idRole);
        if (role == null) {
            throw new IllegalArgumentException("Role nao encontrada");
        }

        validarIdsPermissao(idsPermissao);
        rolePermissaoDAO.substituirPermissoesDaRole(idRole, idsPermissao);
        return obterPermissoesDaRole(idRole);
    }

    public Map<String, Object> obterPermissoesDoUsuario(Long idUsuario) {
        UsuarioBean usuario = usuarioDAO.buscarPorId(idUsuario);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario nao encontrado");
        }

        Set<Long> idsBasePorRole = new LinkedHashSet<>(rolePermissaoDAO.listarIdsPermissaoPorUsuario(idUsuario));
        Map<Long, Boolean> overrides = usuarioPermissaoDAO.listarOverridesDoUsuario(idUsuario);
        return montarRespostaVinculos("usuario", idUsuario, idsBasePorRole, overrides);
    }

    public Map<String, Object> listarCatalogoAcessos() {
        List<PermissaoGrupoBean> gruposAtivos = permissaoGrupoDAO.listarAtivos();
        List<PermissaoTelaBean> permissoesAtivas = permissaoTelaDAO.listarAtivas();

        Map<String, Map<String, Object>> gruposPorCodigo = new LinkedHashMap<>();

        for (PermissaoGrupoBean grupo : gruposAtivos) {
            Map<String, Object> grupoJson = new LinkedHashMap<>();
            grupoJson.put(KEY_GRUPO_CODIGO, grupo.getCodigoGrupo());
            grupoJson.put(KEY_GRUPO_NOME, grupo.getNomeGrupo());
            grupoJson.put(KEY_DESCRICAO, grupo.getDescricao());
            grupoJson.put(KEY_TELAS, new ArrayList<Map<String, Object>>());
            gruposPorCodigo.put(grupo.getCodigoGrupo(), grupoJson);
        }

        Map<String, Map<String, Object>> telasPorChave = new LinkedHashMap<>();

        for (PermissaoTelaBean permissao : permissoesAtivas) {
            String grupoCodigo = permissao.getGrupoCodigo();
            if (grupoCodigo == null || grupoCodigo.isBlank()) {
                grupoCodigo = GRUPO_PADRAO_CODIGO;
            }

            // Implementar getOrDefault manualmente: se não existe o grupo, criar
            if (!gruposPorCodigo.containsKey(grupoCodigo)) {
                Map<String, Object> grupoFallback = new LinkedHashMap<>();
                grupoFallback.put(KEY_GRUPO_CODIGO, grupoCodigo);
                grupoFallback.put(KEY_GRUPO_NOME, permissao.getGrupoNome() != null ? permissao.getGrupoNome() : grupoCodigo);
                grupoFallback.put(KEY_DESCRICAO, null);
                grupoFallback.put(KEY_TELAS, new ArrayList<Map<String, Object>>());
                gruposPorCodigo.put(grupoCodigo, grupoFallback);
            }

            // Obter grupo (agora garantido que existe)
            Map<String, Object> grupoJson = gruposPorCodigo.get(grupoCodigo);

            String telaKey = grupoCodigo + "|" + permissao.getTelaCodigo();
            if (!telasPorChave.containsKey(telaKey)) {
                Map<String, Object> tela = new LinkedHashMap<>();
                tela.put(KEY_GRUPO_CODIGO, grupoCodigo);
                tela.put(KEY_GRUPO_NOME, permissao.getGrupoNome() != null ? permissao.getGrupoNome() : grupoCodigo);
                tela.put(KEY_TELA_CODIGO, permissao.getTelaCodigo());
                tela.put(KEY_TELA_NOME, TELAS_ADMIN.getOrDefault(permissao.getTelaCodigo(), permissao.getTelaCodigo()));
                tela.put(KEY_PERMISSOES, new ArrayList<Map<String, Object>>());
                
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> telasDoGrupo = (List<Map<String, Object>>) grupoJson.get(KEY_TELAS);
                telasDoGrupo.add(tela);
                
                telasPorChave.put(telaKey, tela);
            }

            Map<String, Object> telaJson = telasPorChave.get(telaKey);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> permissoesDaTela = (List<Map<String, Object>>) telaJson.get(KEY_PERMISSOES);

            Map<String, Object> permissaoJson = new LinkedHashMap<>();
            permissaoJson.put(KEY_ID_PERMISSAO, permissao.getIdPermissao());
            permissaoJson.put(KEY_ACAO_CODIGO, permissao.getAcaoCodigo());
            permissaoJson.put(KEY_DESCRICAO, permissao.getDescricao());
            permissoesDaTela.add(permissaoJson);
        }

        List<Map<String, Object>> grupos = new ArrayList<>(gruposPorCodigo.values());
        List<Map<String, Object>> telas = new ArrayList<>();
        for (Map<String, Object> grupo : grupos) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> telasDoGrupo = (List<Map<String, Object>>) grupo.get(KEY_TELAS);
            telas.addAll(telasDoGrupo);
        }

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put(KEY_GRUPOS, grupos);
        resposta.put(KEY_TELAS, telas);
        return resposta;
    }

    public Map<String, Object> atualizarPermissoesDoUsuario(Long idUsuario, List<Map<String, Object>> overrides) {
        UsuarioBean usuario = usuarioDAO.buscarPorId(idUsuario);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario nao encontrado");
        }

        List<Map<String, Object>> normalizados = new ArrayList<>();
        for (Map<String, Object> item : overrides) {
            Object idValue = item.get("idPermissao");
            if (idValue == null) {
                throw new IllegalArgumentException("idPermissao obrigatorio no override");
            }
            Long idPermissao = (idValue instanceof Number n) ? n.longValue() : Long.parseLong(String.valueOf(idValue));
            PermissaoTelaBean permissao = permissaoTelaDAO.buscarPorId(idPermissao);
            if (permissao == null) {
                throw new IllegalArgumentException("Permissao nao encontrada: " + idPermissao);
            }

            Object permitidoValue = item.get("permitido");
            boolean permitido = permitidoValue instanceof Boolean b ? b : Boolean.parseBoolean(String.valueOf(permitidoValue));

            Map<String, Object> row = new HashMap<>();
            row.put("idPermissao", idPermissao);
            row.put("permitido", permitido);
            normalizados.add(row);
        }

        usuarioPermissaoDAO.substituirPermissoesDoUsuario(idUsuario, normalizados);
        return obterPermissoesDoUsuario(idUsuario);
    }

    public Map<String, Object> consultarPermissoesDiretasPorId(Long idUsuario) {
        UsuarioBean usuario = usuarioDAO.buscarPorId(idUsuario);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario nao encontrado");
        }
        return montarRespostaPermissoesDiretas(usuario);
    }

    public Map<String, Object> consultarPermissoesDiretasPorEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email obrigatorio");
        }

        UsuarioBean usuario = usuarioDAO.buscarPorEmail(email.trim());
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario nao encontrado");
        }
        return montarRespostaPermissoesDiretas(usuario);
    }

    public Map<String, Object> obterAcessoEfetivoDoUsuario(Long idUsuario, String telaCodigo) {
        UsuarioBean usuario = usuarioDAO.buscarPorId(idUsuario);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario nao encontrado");
        }

        String telaNormalizada = normalizarTelaCodigo(telaCodigo);
        List<PermissaoTelaBean> permissoesDaTela = permissaoTelaDAO.listarPorTela(telaNormalizada);
        if (permissoesDaTela.isEmpty()) {
            throw new IllegalArgumentException("Tela nao encontrada no catalogo de permissao: " + telaNormalizada);
        }

        Set<Long> idsBasePorRole = new LinkedHashSet<>(rolePermissaoDAO.listarIdsPermissaoPorUsuario(idUsuario));
        Map<Long, Boolean> overrides = usuarioPermissaoDAO.listarOverridesDoUsuario(idUsuario);

        Map<String, Boolean> acoes = new LinkedHashMap<>();
        for (PermissaoTelaBean permissao : permissoesDaTela) {
            boolean porRole = idsBasePorRole.contains(permissao.getIdPermissao());
            Boolean override = overrides.get(permissao.getIdPermissao());

            // User override precedence: if user has an explicit override (true/false),
            // it wins. Otherwise fallback to role permission.
            boolean permitido;
            if (override != null) {
                permitido = override;
            } else {
                permitido = porRole;
            }
            acoes.put(permissao.getAcaoCodigo(), permitido);
        }

        return Map.of(
                "idUsuario", idUsuario,
                "telaCodigo", telaNormalizada,
                "telaNome", TELAS_ADMIN.getOrDefault(telaNormalizada, telaNormalizada),
                "acoes", acoes,
                "podeConsultar", acoes.getOrDefault("CONSULTAR", false)
        );
    }

    public Map<String, Object> excluirTelasCatalogo(List<Long> idsPermissao) {
        if (idsPermissao == null || idsPermissao.isEmpty()) {
            throw new IllegalArgumentException("ids obrigatorios");
        }

        // remover vínculos em roles e usuarios
        rolePermissaoDAO.excluirPermissoes(idsPermissao);
        usuarioPermissaoDAO.excluirPermissoes(idsPermissao);

        // deletar as permissoes de tela
        permissaoTelaDAO.excluirMultiplos(idsPermissao);

        // retornar catálogo atualizado
        return listarCatalogoAcessos();
    }

    public String resolverTelaPorRota(String rota) {
        if (rota == null || rota.isBlank()) {
            return "ADMIN_DASHBOARD";
        }

        String valor = rota.trim().toLowerCase(Locale.ROOT);
        if (valor.equals("/admin") || valor.equals("/admin/")) {
            return "ADMIN_DASHBOARD";
        }
        if (valor.contains("/admin/users")) {
            return "ADMIN_USERS";
        }
        if (valor.contains("/admin/roles")) {
            return "ADMIN_ROLES";
        }
        if (valor.contains("/admin/access-control")) {
            return "ADMIN_ACCESS_CONTROL";
        }
        if (valor.contains("/admin/configs")) {
            return "ADMIN_CONFIGS";
        }
        if (valor.contains("/admin/access-lists")) {
            return "ADMIN_ACCESS_LISTS";
        }
        if (valor.contains("/admin/logs")) {
            return "ADMIN_ACCESS_LOGS";
        }
        return "ADMIN_DASHBOARD";
    }

    private Map<String, Object> montarRespostaVinculos(String tipo, Long idAlvo, Set<Long> idsBasePorRole, Map<Long, Boolean> overrides) {
        List<PermissaoTelaBean> permissoes = permissaoTelaDAO.listarAtivas();
        List<Map<String, Object>> itens = new ArrayList<>();

        for (PermissaoTelaBean permissao : permissoes) {
            boolean porRole = idsBasePorRole.contains(permissao.getIdPermissao());
            Boolean override = overrides.get(permissao.getIdPermissao());

            // User override precedence: if user has an explicit override (true/false),
            // it wins. Otherwise fallback to role permission.
            boolean efetivo;
            if (override != null) {
                efetivo = override;
            } else {
                efetivo = porRole;
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("idPermissao", permissao.getIdPermissao());
            item.put("telaCodigo", permissao.getTelaCodigo());
            item.put("acaoCodigo", permissao.getAcaoCodigo());
            item.put("descricao", permissao.getDescricao());
            item.put("porRole", porRole);
            item.put("overrideUsuario", override);
            item.put("efetivo", efetivo);
            itens.add(item);
        }

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("tipo", tipo);
        resposta.put("id", idAlvo);
        resposta.put("permissoes", itens);
        return resposta;
    }

    private Map<String, Object> montarRespostaPermissoesDiretas(UsuarioBean usuario) {
        Long idUsuario = usuario.getIdUsuario();
        Map<Long, Boolean> overrides = usuarioPermissaoDAO.listarOverridesDoUsuario(idUsuario);
        Set<Long> idsPorRole = new LinkedHashSet<>(rolePermissaoDAO.listarIdsPermissaoPorUsuario(idUsuario));
        List<PermissaoTelaBean> catalogo = permissaoTelaDAO.listarAtivas();

        List<Map<String, Object>> permissoesDiretas = new ArrayList<>();
        for (PermissaoTelaBean permissao : catalogo) {
            Boolean permitidoDireto = overrides.get(permissao.getIdPermissao());
            // include both explicit allows and explicit denies so admin UI can show them
            if (permitidoDireto == null) {
                continue;
            }

            boolean porRole = idsPorRole.contains(permissao.getIdPermissao());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("idPermissao", permissao.getIdPermissao());
            item.put("telaCodigo", permissao.getTelaCodigo());
            item.put("telaNome", TELAS_ADMIN.getOrDefault(permissao.getTelaCodigo(), permissao.getTelaCodigo()));
            item.put("acaoCodigo", permissao.getAcaoCodigo());
            item.put("descricao", permissao.getDescricao());
            item.put("direta", true);
            item.put("porRole", porRole);
            // efetivo segue override when explicit
            item.put("efetivo", permitidoDireto);
            item.put("permitido", permitidoDireto);
            permissoesDiretas.add(item);
        }

        Map<String, Object> usuarioJson = new LinkedHashMap<>();
        usuarioJson.put("idUsuario", idUsuario);
        usuarioJson.put("nome", usuario.getNome());
        usuarioJson.put("email", usuario.getEmail());
        usuarioJson.put("ativo", usuario.getAtivo());
        usuarioJson.put("roles", usuarioRoleDAO.listarNomesRolesDoUsuario(idUsuario));

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("tipo", "usuario_permissoes_diretas");
        resposta.put("usuario", usuarioJson);
        resposta.put("totalPermissoesDiretas", permissoesDiretas.size());
        resposta.put("permissoes", permissoesDiretas);
        return resposta;
    }

    private void validarIdsPermissao(List<Long> idsPermissao) {
        for (Long idPermissao : idsPermissao) {
            if (permissaoTelaDAO.buscarPorId(idPermissao) == null) {
                throw new IllegalArgumentException("Permissao nao encontrada: " + idPermissao);
            }
        }
    }

    private String normalizarTelaCodigo(String telaCodigo) {
        if (telaCodigo == null || telaCodigo.isBlank()) {
            throw new IllegalArgumentException("telaCodigo obrigatorio");
        }
        return telaCodigo.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizarGrupoCodigo(String grupoCodigo) {
        if (grupoCodigo == null || grupoCodigo.isBlank()) {
            throw new IllegalArgumentException("grupoCodigo obrigatorio");
        }
        return grupoCodigo.trim().toUpperCase(Locale.ROOT);
    }

    private PermissaoGrupoBean garantirGrupoPadrao() {
        PermissaoGrupoBean grupo = permissaoGrupoDAO.buscarPorCodigo(GRUPO_PADRAO_CODIGO);
        if (grupo != null) {
            return grupo;
        }

        PermissaoGrupoBean novoGrupo = new PermissaoGrupoBean();
        novoGrupo.setCodigoGrupo(GRUPO_PADRAO_CODIGO);
        novoGrupo.setNomeGrupo(GRUPO_PADRAO_NOME);
        novoGrupo.setDescricao("Grupo padrao do painel administrativo");
        novoGrupo.setAtivo(true);
        Long idGrupo = permissaoGrupoDAO.inserir(novoGrupo);
        return permissaoGrupoDAO.buscarPorId(idGrupo);
    }
}
