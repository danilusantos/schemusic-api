package br.com.schemusic.schemusic_api.business;

import br.com.schemusic.schemusic_api.bean.PermissaoTelaBean;
import br.com.schemusic.schemusic_api.bean.RoleBean;
import br.com.schemusic.schemusic_api.bean.UsuarioBean;
import br.com.schemusic.schemusic_api.dao.PermissaoTelaDAO;
import br.com.schemusic.schemusic_api.dao.RoleDAO;
import br.com.schemusic.schemusic_api.dao.RolePermissaoDAO;
import br.com.schemusic.schemusic_api.dao.UsuarioDAO;
import br.com.schemusic.schemusic_api.dao.UsuarioPermissaoDAO;
import br.com.schemusic.schemusic_api.dao.UsuarioRoleDAO;
import org.springframework.stereotype.Service;

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

    private static final Map<String, String> TELAS_ADMIN = Map.ofEntries(
            Map.entry("ADMIN_DASHBOARD", "Dashboard administrativo"),
            Map.entry("ADMIN_USERS", "Usuarios administrativos"),
            Map.entry("ADMIN_ROLES", "Perfis de acesso"),
            Map.entry("ADMIN_ACCESS_CONTROL", "Controle de permissoes"),
            Map.entry("ADMIN_CONFIGS", "Configuracoes do sistema"),
            Map.entry("ADMIN_ACCESS_LISTS", "Listas de acesso"),
            Map.entry("ADMIN_ACCESS_LOGS", "Logs de acesso")
    );

    private final PermissaoTelaDAO permissaoTelaDAO;
    private final RolePermissaoDAO rolePermissaoDAO;
    private final UsuarioPermissaoDAO usuarioPermissaoDAO;
    private final UsuarioDAO usuarioDAO;
    private final RoleDAO roleDAO;
    private final UsuarioRoleDAO usuarioRoleDAO;

    public AdminAccessControlBusiness(PermissaoTelaDAO permissaoTelaDAO,
                                      RolePermissaoDAO rolePermissaoDAO,
                                      UsuarioPermissaoDAO usuarioPermissaoDAO,
                                      UsuarioDAO usuarioDAO,
                                      RoleDAO roleDAO,
                                      UsuarioRoleDAO usuarioRoleDAO) {
        this.permissaoTelaDAO = permissaoTelaDAO;
        this.rolePermissaoDAO = rolePermissaoDAO;
        this.usuarioPermissaoDAO = usuarioPermissaoDAO;
        this.usuarioDAO = usuarioDAO;
        this.roleDAO = roleDAO;
        this.usuarioRoleDAO = usuarioRoleDAO;
    }

    public void criarCatalogoPadraoSeNecessario() {
        for (Map.Entry<String, String> tela : TELAS_ADMIN.entrySet()) {
            for (String acao : ACOES_PADRAO) {
                if (permissaoTelaDAO.buscarPorTelaEAcao(tela.getKey(), acao) != null) {
                    continue;
                }
                PermissaoTelaBean permissao = new PermissaoTelaBean();
                permissao.setTelaCodigo(tela.getKey());
                permissao.setAcaoCodigo(acao);
                permissao.setDescricao(tela.getValue() + " - " + acao);
                permissao.setAtivo(true);
                permissaoTelaDAO.inserir(permissao);
            }
        }
    }

    public Map<String, Object> listarCatalogo() {
        List<PermissaoTelaBean> permissoes = permissaoTelaDAO.listarAtivas();
        Map<String, Object> telas = new LinkedHashMap<>();

        for (PermissaoTelaBean permissao : permissoes) {
            String telaCodigo = permissao.getTelaCodigo();
            @SuppressWarnings("unchecked")
            Map<String, Object> tela = (Map<String, Object>) telas.computeIfAbsent(telaCodigo, key -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("telaCodigo", key);
                item.put("telaNome", TELAS_ADMIN.getOrDefault(key, key));
                item.put("permissoes", new ArrayList<Map<String, Object>>());
                return item;
            });

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> listaPermissoes = (List<Map<String, Object>>) tela.get("permissoes");
            Map<String, Object> permissaoJson = new LinkedHashMap<>();
            permissaoJson.put("idPermissao", permissao.getIdPermissao());
            permissaoJson.put("acaoCodigo", permissao.getAcaoCodigo());
            permissaoJson.put("descricao", permissao.getDescricao());
            listaPermissoes.add(permissaoJson);
        }

        return Map.of(
                "telas", new ArrayList<>(telas.values())
        );
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

            // Regra OR: acesso efetivo pode vir do perfil OU do usuario.
            boolean permitido = porRole || Boolean.TRUE.equals(override);
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

            // Regra OR: efetivo quando perfil permite ou usuario possui permissao direta.
            boolean efetivo = porRole || Boolean.TRUE.equals(override);

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
            if (!Boolean.TRUE.equals(permitidoDireto)) {
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
            item.put("efetivo", true);
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
}
