package br.com.schemusic.schemusic_api.config;

import br.com.schemusic.schemusic_api.bean.RoleBean;
import br.com.schemusic.schemusic_api.bean.UsuarioBean;
import br.com.schemusic.schemusic_api.business.AdminAccessControlBusiness;
import br.com.schemusic.schemusic_api.dao.AcessoSiteLogDAO;
import br.com.schemusic.schemusic_api.dao.ListaAcessoDAO;
import br.com.schemusic.schemusic_api.dao.PermissaoTelaDAO;
import br.com.schemusic.schemusic_api.dao.RoleDAO;
import br.com.schemusic.schemusic_api.dao.PermissaoGrupoDAO;
import br.com.schemusic.schemusic_api.dao.RolePermissaoDAO;
import br.com.schemusic.schemusic_api.dao.SistemaConfigDAO;
import br.com.schemusic.schemusic_api.dao.UsuarioDAO;
import br.com.schemusic.schemusic_api.dao.UsuarioPermissaoDAO;
import br.com.schemusic.schemusic_api.dao.UsuarioRoleDAO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeedInitializer implements CommandLineRunner {

    private final UsuarioDAO usuarioDAO;
    private final RoleDAO roleDAO;
    private final UsuarioRoleDAO usuarioRoleDAO;
    private final PermissaoTelaDAO permissaoTelaDAO;
    private final RolePermissaoDAO rolePermissaoDAO;
    private final PermissaoGrupoDAO permissaoGrupoDAO;
    private final UsuarioPermissaoDAO usuarioPermissaoDAO;
    private final AdminAccessControlBusiness adminAccessControlBusiness;
    private final SistemaConfigDAO sistemaConfigDAO;
    private final ListaAcessoDAO listaAcessoDAO;
    private final AcessoSiteLogDAO acessoSiteLogDAO;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.seed-enabled:true}")
    private boolean seedEnabled;

    @Value("${app.admin.nome:Administrador}")
    private String adminNome;

    @Value("${app.admin.email:admin@schemusic.com}")
    private String adminEmail;

    @Value("${app.admin.senha:admin123}")
    private String adminSenha;

    @Value("${app.admin.dev.nome:dev_schemusic}")
    private String devNome;

    @Value("${app.admin.dev.email:dev_schemusic@schemusic.com}")
    private String devEmail;

    @Value("${app.admin.dev.senha:dev123}")
    private String devSenha;

    public AdminSeedInitializer(UsuarioDAO usuarioDAO,
                                RoleDAO roleDAO,
                                UsuarioRoleDAO usuarioRoleDAO,
                                PermissaoTelaDAO permissaoTelaDAO,
                                RolePermissaoDAO rolePermissaoDAO,
                                UsuarioPermissaoDAO usuarioPermissaoDAO,
                                PermissaoGrupoDAO permissaoGrupoDAO,
                                AdminAccessControlBusiness adminAccessControlBusiness,
                                SistemaConfigDAO sistemaConfigDAO,
                                ListaAcessoDAO listaAcessoDAO,
                                AcessoSiteLogDAO acessoSiteLogDAO,
                                PasswordEncoder passwordEncoder) {
        this.usuarioDAO = usuarioDAO;
        this.roleDAO = roleDAO;
        this.usuarioRoleDAO = usuarioRoleDAO;
        this.permissaoTelaDAO = permissaoTelaDAO;
        this.rolePermissaoDAO = rolePermissaoDAO;
        this.usuarioPermissaoDAO = usuarioPermissaoDAO;
        this.permissaoGrupoDAO = permissaoGrupoDAO;
        this.adminAccessControlBusiness = adminAccessControlBusiness;
        this.sistemaConfigDAO = sistemaConfigDAO;
        this.listaAcessoDAO = listaAcessoDAO;
        this.acessoSiteLogDAO = acessoSiteLogDAO;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        roleDAO.criarTabelaSeNaoExistir();
        usuarioDAO.criarTabelaSeNaoExistir();
        usuarioRoleDAO.criarTabelaSeNaoExistir();
        permissaoTelaDAO.criarTabelaSeNaoExistir();
        permissaoGrupoDAO.criarTabelaSeNaoExistir();
        rolePermissaoDAO.criarTabelaSeNaoExistir();
        usuarioPermissaoDAO.criarTabelaSeNaoExistir();
        sistemaConfigDAO.criarTabelaSeNaoExistir();
        listaAcessoDAO.criarTabelaSeNaoExistir();
        acessoSiteLogDAO.criarTabelaSeNaoExistir();
        adminAccessControlBusiness.criarCatalogoPadraoSeNecessario();

        roleDAO.criarRoleSeNaoExistir("ADMIN", "Administracao do sistema");
        roleDAO.criarRoleSeNaoExistir("ARTISTA", "Usuario artista");
        roleDAO.criarRoleSeNaoExistir("EMPRESA", "Usuario empresa");

        garantirPermissoesRoleAdmin();

        sistemaConfigDAO.criarOuAtualizarPorChave(
                "SYSTEM_DEFAULT_EMAIL",
                "nao-responder@schemusic.com",
                "Email padrao do sistema"
        );
        sistemaConfigDAO.criarOuAtualizarPorChave(
                "SYSTEM_DEADLINE_DAYS",
                "7",
                "Prazo padrao em dias para operacoes com data limite"
        );

        if (!seedEnabled) {
            return;
        }

        UsuarioBean existente = usuarioDAO.buscarPorEmail(adminEmail);
        if (existente != null) {
            garantirVinculoAdmin(existente.getIdUsuario());
        } else {
            UsuarioBean admin = new UsuarioBean();
            admin.setNome(adminNome);
            admin.setEmail(adminEmail);
            admin.setSenha(passwordEncoder.encode(adminSenha));
            admin.setIdiomaPadrao("pt-BR");
            admin.setAtivo(true);

            Long idUsuario = usuarioDAO.inserir(admin);
            garantirVinculoAdmin(idUsuario);
        }

        criarOuAtualizarUsuarioDev();
    }

    private void criarOuAtualizarUsuarioDev() {
        UsuarioBean existenteDev = usuarioDAO.buscarPorEmail(devEmail);
        if (existenteDev == null) {
            UsuarioBean dev = new UsuarioBean();
            dev.setNome(devNome);
            dev.setEmail(devEmail);
            dev.setSenha(passwordEncoder.encode(devSenha));
            dev.setIdiomaPadrao("pt-BR");
            dev.setAtivo(true);

            Long idUsuario = usuarioDAO.inserir(dev);
            garantirVinculoAdmin(idUsuario);
            return;
        }

        garantirVinculoAdmin(existenteDev.getIdUsuario());
    }

    private void garantirVinculoAdmin(Long idUsuario) {
        RoleBean roleAdmin = obterRoleAdminObrigatoria();
        usuarioRoleDAO.vincularSeNaoExistir(idUsuario, roleAdmin.getIdRole());

        java.util.List<Long> idsPermissaoAtivas = listarIdsPermissaoAtivas();

        java.util.Map<Long, Boolean> overridesAdmin = usuarioPermissaoDAO.listarOverridesDoUsuario(idUsuario);
        boolean adminPadraoTemTodasPermissoes = idsPermissaoAtivas.stream().allMatch(id -> Boolean.TRUE.equals(overridesAdmin.get(id)));
        if (!adminPadraoTemTodasPermissoes) {
            java.util.List<java.util.Map<String, Object>> permissoesAdminPadrao = idsPermissaoAtivas.stream()
                    .map(idPermissao -> {
                        java.util.Map<String, Object> item = new java.util.HashMap<>();
                        item.put("idPermissao", idPermissao);
                        item.put("permitido", true);
                        return item;
                    })
                    .collect(java.util.stream.Collectors.toList());
            usuarioPermissaoDAO.substituirPermissoesDoUsuario(idUsuario, permissoesAdminPadrao);
        }
    }

    private void garantirPermissoesRoleAdmin() {
        RoleBean roleAdmin = obterRoleAdminObrigatoria();
        java.util.List<Long> idsPermissaoAtivas = listarIdsPermissaoAtivas();
        java.util.List<Long> idsPermissaoRoleAdmin = rolePermissaoDAO.listarIdsPermissaoDaRole(roleAdmin.getIdRole());
        if (!idsPermissaoRoleAdmin.containsAll(idsPermissaoAtivas)) {
            java.util.LinkedHashSet<Long> merge = new java.util.LinkedHashSet<>(idsPermissaoRoleAdmin);
            merge.addAll(idsPermissaoAtivas);
            rolePermissaoDAO.substituirPermissoesDaRole(roleAdmin.getIdRole(), new java.util.ArrayList<>(merge));
        }
    }

    private RoleBean obterRoleAdminObrigatoria() {
        RoleBean roleAdmin = roleDAO.buscarPorNome("ADMIN");
        if (roleAdmin == null) {
            throw new IllegalStateException("Role ADMIN nao encontrada para vincular no seed");
        }
        return roleAdmin;
    }

    private java.util.List<Long> listarIdsPermissaoAtivas() {
        return permissaoTelaDAO.listarAtivas().stream()
                .map(p -> p.getIdPermissao())
                .collect(java.util.stream.Collectors.toList());
    }
}
