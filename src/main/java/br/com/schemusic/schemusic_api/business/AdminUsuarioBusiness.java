package br.com.schemusic.schemusic_api.business;

import br.com.schemusic.schemusic_api.bean.UsuarioBean;
import br.com.schemusic.schemusic_api.dao.RoleDAO;
import br.com.schemusic.schemusic_api.dao.UsuarioDAO;
import br.com.schemusic.schemusic_api.dao.UsuarioRoleDAO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.List;

@Service
public class AdminUsuarioBusiness {

    private static final String DEFAULT_LANGUAGE = "pt-BR";
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of(DEFAULT_LANGUAGE, "en-US", "es-ES");

    private final UsuarioDAO usuarioDAO;
    private final RoleDAO roleDAO;
    private final UsuarioRoleDAO usuarioRoleDAO;
    private final PasswordEncoder passwordEncoder;

    public AdminUsuarioBusiness(UsuarioDAO usuarioDAO,
                                RoleDAO roleDAO,
                                UsuarioRoleDAO usuarioRoleDAO,
                                PasswordEncoder passwordEncoder) {
        this.usuarioDAO = usuarioDAO;
        this.roleDAO = roleDAO;
        this.usuarioRoleDAO = usuarioRoleDAO;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioBean criarAdmin(String nome, String email, String senha, String idiomaPadrao) {
        validarDadosMinimos(nome, email, senha);
        if (usuarioDAO.buscarPorEmail(email) != null) {
            throw new IllegalArgumentException("Email ja cadastrado");
        }

        UsuarioBean usuario = new UsuarioBean();
        usuario.setNome(nome.trim());
        usuario.setEmail(email.trim().toLowerCase());
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setIdiomaPadrao(normalizarIdiomaPadrao(idiomaPadrao));
        usuario.setAtivo(true);

        Long id = usuarioDAO.inserir(usuario);
        var roleAdmin = roleDAO.buscarPorNome("ADMIN");
        if (roleAdmin == null) {
            throw new IllegalStateException("Role ADMIN nao encontrada");
        }
        usuarioRoleDAO.vincularSeNaoExistir(id, roleAdmin.getIdRole());

        UsuarioBean criado = usuarioDAO.buscarPorId(id);
        criado.setRoles(usuarioRoleDAO.listarNomesRolesDoUsuario(id));
        return criado;
    }

    public UsuarioBean editar(Long id, String nome, String email, String senha, String idiomaPadrao, Boolean ativo) {
        UsuarioBean existente = usuarioDAO.buscarPorId(id);
        if (existente == null) {
            throw new IllegalArgumentException("Usuario nao encontrado");
        }

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome obrigatorio");
        }
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Email invalido");
        }

        UsuarioBean porEmail = usuarioDAO.buscarPorEmail(email.trim().toLowerCase());
        if (porEmail != null && !porEmail.getIdUsuario().equals(id)) {
            throw new IllegalArgumentException("Email ja esta em uso");
        }

        existente.setNome(nome.trim());
        existente.setEmail(email.trim().toLowerCase());
        if (idiomaPadrao != null) {
            existente.setIdiomaPadrao(normalizarIdiomaPadrao(idiomaPadrao));
        }
        existente.setAtivo(ativo != null ? ativo : existente.getAtivo());
        if (senha != null && !senha.isBlank()) {
            if (senha.length() < 6) {
                throw new IllegalArgumentException("Senha deve ter no minimo 6 caracteres");
            }
            existente.setSenha(passwordEncoder.encode(senha));
        } else {
            existente.setSenha(null);
        }

        usuarioDAO.atualizar(existente);
        UsuarioBean atualizado = usuarioDAO.buscarPorId(id);
        atualizado.setRoles(usuarioRoleDAO.listarNomesRolesDoUsuario(id));
        return atualizado;
    }

    public UsuarioBean atualizarIdiomaPadrao(Long id, String idiomaPadrao) {
        UsuarioBean existente = usuarioDAO.buscarPorId(id);
        if (existente == null) {
            throw new IllegalArgumentException("Usuario nao encontrado");
        }

        existente.setIdiomaPadrao(normalizarIdiomaPadrao(idiomaPadrao));
        usuarioDAO.atualizar(existente);

        UsuarioBean atualizado = usuarioDAO.buscarPorId(id);
        atualizado.setRoles(usuarioRoleDAO.listarNomesRolesDoUsuario(id));
        return atualizado;
    }

    public UsuarioBean vincularRoles(Long idUsuario, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new IllegalArgumentException("Informe ao menos uma role");
        }
        UsuarioBean usuario = usuarioDAO.buscarPorId(idUsuario);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario nao encontrado");
        }

        for (Long roleId : roleIds) {
            if (roleDAO.buscarPorId(roleId) == null) {
                throw new IllegalArgumentException("Role nao encontrada: " + roleId);
            }
        }

        usuarioRoleDAO.substituirRolesDoUsuario(idUsuario, roleIds);
        UsuarioBean atualizado = usuarioDAO.buscarPorId(idUsuario);
        atualizado.setRoles(usuarioRoleDAO.listarNomesRolesDoUsuario(idUsuario));
        return atualizado;
    }

    public void inativar(Long idUsuario) {
        UsuarioBean usuario = usuarioDAO.buscarPorId(idUsuario);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario nao encontrado");
        }
        usuarioDAO.inativar(idUsuario);
    }

    public void ativar(Long idUsuario) {
        UsuarioBean usuario = usuarioDAO.buscarPorId(idUsuario);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario nao encontrado");
        }
        usuarioDAO.ativar(idUsuario);
    }

    public List<UsuarioBean> listar(Boolean ativo, String termo, Long roleId) {
        return usuarioDAO.listar(ativo, termo, roleId);
    }

    private void validarDadosMinimos(String nome, String email, String senha) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome obrigatorio");
        }
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Email invalido");
        }
        if (senha == null || senha.length() < 6) {
            throw new IllegalArgumentException("Senha deve ter no minimo 6 caracteres");
        }
    }

    private String normalizarIdiomaPadrao(String idiomaPadrao) {
        if (idiomaPadrao == null || idiomaPadrao.isBlank()) {
            return DEFAULT_LANGUAGE;
        }

        String idiomaNormalizado = idiomaPadrao.trim();
        if (!SUPPORTED_LANGUAGES.contains(idiomaNormalizado)) {
            throw new IllegalArgumentException("Idioma padrao invalido. Use: pt-BR, en-US ou es-ES");
        }

        return idiomaNormalizado;
    }
}
