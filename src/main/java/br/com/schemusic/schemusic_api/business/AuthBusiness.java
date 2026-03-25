package br.com.schemusic.schemusic_api.business;

import br.com.schemusic.schemusic_api.bean.LoginResponseBean;
import br.com.schemusic.schemusic_api.bean.UsuarioBean;
import br.com.schemusic.schemusic_api.dao.UsuarioDAO;
import br.com.schemusic.schemusic_api.dao.UsuarioRoleDAO;
import br.com.schemusic.schemusic_api.exception.AuthenticationException;
import br.com.schemusic.schemusic_api.util.JwtUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthBusiness {

    private static final String ROLE_ARTISTA = "ARTISTA";
    private static final String ROLE_ADMIN = "ADMIN";

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UsuarioDAO usuarioDAO;
    private final UsuarioRoleDAO usuarioRoleDAO;

    private static final Long ARTISTA_ID = 1L;
    private static final String ARTISTA_NOME = "Artista Demo";
    private static final String ARTISTA_EMAIL = "artista@schemusic.com";
    private static final String ARTISTA_SENHA = "123456";
    private static final String DEFAULT_LANGUAGE = "pt-BR";

    private String artistaSenhaHash;

    public AuthBusiness(PasswordEncoder passwordEncoder,
                        JwtUtil jwtUtil,
                        UsuarioDAO usuarioDAO,
                        UsuarioRoleDAO usuarioRoleDAO) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.usuarioDAO = usuarioDAO;
        this.usuarioRoleDAO = usuarioRoleDAO;
    }

    @PostConstruct
    public void init() {
        this.artistaSenhaHash = passwordEncoder.encode(ARTISTA_SENHA);
    }

    public LoginResponseBean loginArtista(String email, String senha) {
        if (!ARTISTA_EMAIL.equalsIgnoreCase(email)) {
            throw new AuthenticationException("Credenciais invalidas");
        }

        if (!passwordEncoder.matches(senha, artistaSenhaHash)) {
            throw new AuthenticationException("Credenciais invalidas");
        }

        String token = jwtUtil.generateToken(ARTISTA_ID, ROLE_ARTISTA);
        return new LoginResponseBean(ARTISTA_ID, ARTISTA_NOME, ROLE_ARTISTA, token, DEFAULT_LANGUAGE);
    }

    public LoginResponseBean loginAdministrador(String email, String senha) {
        UsuarioBean usuario = usuarioDAO.buscarAtivoPorEmail(email);
        if (usuario == null) {
            throw new AuthenticationException("Credenciais invalidas");
        }

        if (!passwordEncoder.matches(senha, usuario.getSenha())) {
            throw new AuthenticationException("Credenciais invalidas");
        }

        if (!usuarioRoleDAO.usuarioPossuiRole(usuario.getIdUsuario(), ROLE_ADMIN)) {
            throw new AuthenticationException("Credenciais invalidas");
        }

        String token = jwtUtil.generateToken(usuario.getIdUsuario(), ROLE_ADMIN);
        return new LoginResponseBean(
                usuario.getIdUsuario(),
                usuario.getNome(),
            ROLE_ADMIN,
            token,
            usuario.getIdiomaPadrao() != null ? usuario.getIdiomaPadrao() : DEFAULT_LANGUAGE
        );
    }
}
