package br.com.schemusic.schemusic_api.config;

import br.com.schemusic.schemusic_api.bean.RoleBean;
import br.com.schemusic.schemusic_api.bean.UsuarioBean;
import br.com.schemusic.schemusic_api.dao.AcessoSiteLogDAO;
import br.com.schemusic.schemusic_api.dao.ListaAcessoDAO;
import br.com.schemusic.schemusic_api.dao.RoleDAO;
import br.com.schemusic.schemusic_api.dao.SistemaConfigDAO;
import br.com.schemusic.schemusic_api.dao.UsuarioDAO;
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

    public AdminSeedInitializer(UsuarioDAO usuarioDAO,
                                RoleDAO roleDAO,
                                UsuarioRoleDAO usuarioRoleDAO,
                                SistemaConfigDAO sistemaConfigDAO,
                                ListaAcessoDAO listaAcessoDAO,
                                AcessoSiteLogDAO acessoSiteLogDAO,
                                PasswordEncoder passwordEncoder) {
        this.usuarioDAO = usuarioDAO;
        this.roleDAO = roleDAO;
        this.usuarioRoleDAO = usuarioRoleDAO;
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
        sistemaConfigDAO.criarTabelaSeNaoExistir();
        listaAcessoDAO.criarTabelaSeNaoExistir();
        acessoSiteLogDAO.criarTabelaSeNaoExistir();

        roleDAO.criarRoleSeNaoExistir("ADMIN", "Administracao do sistema");
        roleDAO.criarRoleSeNaoExistir("ARTISTA", "Usuario artista");
        roleDAO.criarRoleSeNaoExistir("EMPRESA", "Usuario empresa");

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
            return;
        }

        UsuarioBean admin = new UsuarioBean();
        admin.setNome(adminNome);
        admin.setEmail(adminEmail);
        admin.setSenha(passwordEncoder.encode(adminSenha));
        admin.setIdiomaPadrao("pt-BR");
        admin.setAtivo(true);

        Long idUsuario = usuarioDAO.inserir(admin);
        garantirVinculoAdmin(idUsuario);
    }

    private void garantirVinculoAdmin(Long idUsuario) {
        RoleBean roleAdmin = roleDAO.buscarPorNome("ADMIN");
        if (roleAdmin == null) {
            throw new IllegalStateException("Role ADMIN nao encontrada para vincular no seed");
        }
        usuarioRoleDAO.vincularSeNaoExistir(idUsuario, roleAdmin.getIdRole());
    }
}
