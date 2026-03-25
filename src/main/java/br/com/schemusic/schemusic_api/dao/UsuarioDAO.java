package br.com.schemusic.schemusic_api.dao;

import br.com.schemusic.schemusic_api.bean.UsuarioBean;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
public class UsuarioDAO {

    private static final String DEFAULT_LANGUAGE = "pt-BR";
    private static final String COLUMN_IDIOMA_PADRAO = "idioma_padrao";
    private final DataSource dataSource;

    public UsuarioDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void criarTabelaSeNaoExistir() {
        String sql = "CREATE TABLE IF NOT EXISTS USUARIO ("
                + "id_usuario BIGINT AUTO_INCREMENT PRIMARY KEY,"
                + "nome VARCHAR(150) NOT NULL,"
                + "email VARCHAR(100) NOT NULL UNIQUE,"
                + "senha VARCHAR(255) NOT NULL,"
                + "idioma_padrao VARCHAR(10) NOT NULL DEFAULT 'pt-BR',"
                + "ativo BOOLEAN DEFAULT TRUE,"
                + "data_cadastro DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "data_atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                + ")";

        executarDDL(sql, "Erro ao criar tabela USUARIO");
        adicionarColunaIdiomaPadraoSeNecessario();
    }

    public UsuarioBean buscarAtivoPorEmail(String email) {
        String sql = "SELECT id_usuario, nome, email, senha, idioma_padrao, ativo, data_cadastro "
                + "FROM USUARIO WHERE email = ? AND ativo = TRUE LIMIT 1";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
            return null;
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao buscar usuario ativo por email", ex);
        }
    }

    public UsuarioBean buscarPorEmail(String email) {
        String sql = "SELECT id_usuario, nome, email, senha, idioma_padrao, ativo, data_cadastro "
                + "FROM USUARIO WHERE email = ? LIMIT 1";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
            return null;
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao buscar usuario por email", ex);
        }
    }

    public UsuarioBean buscarPorId(Long idUsuario) {
        String sql = "SELECT id_usuario, nome, email, senha, idioma_padrao, ativo, data_cadastro "
                + "FROM USUARIO WHERE id_usuario = ? LIMIT 1";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, idUsuario);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
            return null;
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao buscar usuario por id", ex);
        }
    }

    public Long inserir(UsuarioBean usuario) {
        String sql = "INSERT INTO USUARIO (nome, email, senha, idioma_padrao, ativo) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, usuario.getNome());
            statement.setString(2, usuario.getEmail());
            statement.setString(3, usuario.getSenha());
            statement.setString(4, normalizarIdiomaPadrao(usuario.getIdiomaPadrao()));
            statement.setBoolean(5, Boolean.TRUE.equals(usuario.getAtivo()));
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
            throw new IllegalStateException("Nao foi possivel obter o id do usuario inserido");
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao inserir usuario", ex);
        }
    }

    public void atualizar(UsuarioBean usuario) {
        String sql = "UPDATE USUARIO SET nome = ?, email = ?, senha = COALESCE(?, senha), idioma_padrao = ?, ativo = ? WHERE id_usuario = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, usuario.getNome());
            statement.setString(2, usuario.getEmail());
            statement.setString(3, usuario.getSenha());
            statement.setString(4, normalizarIdiomaPadrao(usuario.getIdiomaPadrao()));
            statement.setBoolean(5, Boolean.TRUE.equals(usuario.getAtivo()));
            statement.setLong(6, usuario.getIdUsuario());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao atualizar usuario", ex);
        }
    }

    public void inativar(Long idUsuario) {
        String sql = "UPDATE USUARIO SET ativo = FALSE WHERE id_usuario = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idUsuario);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao inativar usuario", ex);
        }
    }

    public void ativar(Long idUsuario) {
        String sql = "UPDATE USUARIO SET ativo = TRUE WHERE id_usuario = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idUsuario);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao ativar usuario", ex);
        }
    }
    
    public List<UsuarioBean> listar(Boolean ativo, String termo, Long roleId) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT u.id_usuario, u.nome, u.email, u.ativo, u.data_cadastro, u.idioma_padrao,")
                .append(" GROUP_CONCAT(DISTINCT r.nome ORDER BY r.nome SEPARATOR ',') AS roles ")
                .append("FROM USUARIO u ")
                .append("LEFT JOIN USUARIO_ROLE ur ON ur.id_usuario = u.id_usuario ")
                .append("LEFT JOIN ROLE_SISTEMA r ON r.id_role = ur.id_role ")
                .append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (ativo != null) {
            sql.append("AND u.ativo = ? ");
            params.add(ativo);
        }
        if (termo != null && !termo.isBlank()) {
            sql.append("AND (LOWER(u.nome) LIKE ? OR LOWER(u.email) LIKE ?) ");
            params.add("%" + termo.toLowerCase() + "%");
            params.add("%" + termo.toLowerCase() + "%");
        }
        if (roleId != null) {
            sql.append("AND ur.id_role = ? ");
            params.add(roleId);
        }

        sql.append("GROUP BY u.id_usuario, u.nome, u.email, u.ativo, u.data_cadastro, u.idioma_padrao ")
                .append("ORDER BY u.id_usuario DESC");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }

            List<UsuarioBean> usuarios = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    UsuarioBean usuario = new UsuarioBean();
                    usuario.setIdUsuario(rs.getLong("id_usuario"));
                    usuario.setNome(rs.getString("nome"));
                    usuario.setEmail(rs.getString("email"));
                    usuario.setIdiomaPadrao(normalizarIdiomaPadrao(rs.getString(COLUMN_IDIOMA_PADRAO)));
                    usuario.setAtivo(rs.getBoolean("ativo"));
                    Timestamp dataCadastro = rs.getTimestamp("data_cadastro");
                    if (dataCadastro != null) {
                        usuario.setDataCadastro(dataCadastro.toLocalDateTime());
                    }
                    String roles = rs.getString("roles");
                    if (roles != null && !roles.isBlank()) {
                        usuario.setRoles(Arrays.asList(roles.split(",")));
                    }
                    usuarios.add(usuario);
                }
            }
            return usuarios;
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao listar usuarios", ex);
        }
    }

    public long contar(Boolean ativo) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM USUARIO WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (ativo != null) {
            sql.append("AND ativo = ? ");
            params.add(ativo);
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return 0L;
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao contar usuarios", ex);
        }
    }

    private UsuarioBean mapear(ResultSet rs) throws SQLException {
        UsuarioBean usuario = new UsuarioBean();
        usuario.setIdUsuario(rs.getLong("id_usuario"));
        usuario.setNome(rs.getString("nome"));
        usuario.setEmail(rs.getString("email"));
        usuario.setSenha(rs.getString("senha"));
        usuario.setIdiomaPadrao(normalizarIdiomaPadrao(rs.getString(COLUMN_IDIOMA_PADRAO)));
        usuario.setAtivo(rs.getBoolean("ativo"));
        Timestamp dataCadastro = rs.getTimestamp("data_cadastro");
        if (dataCadastro != null) {
            usuario.setDataCadastro(dataCadastro.toLocalDateTime());
        }
        return usuario;
    }

    private void executarDDL(String sql, String mensagemErro) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        } catch (SQLException ex) {
            throw new IllegalStateException(mensagemErro, ex);
        }
    }

    private void adicionarColunaIdiomaPadraoSeNecessario() {
        try (Connection connection = dataSource.getConnection()) {
            boolean colunaExiste = false;
            try (ResultSet columns = connection.getMetaData().getColumns(null, null, "USUARIO", COLUMN_IDIOMA_PADRAO)) {
                colunaExiste = columns.next();
            }

            if (!colunaExiste) {
                executarDDL(
                        "ALTER TABLE USUARIO ADD COLUMN idioma_padrao VARCHAR(10) NOT NULL DEFAULT 'pt-BR'",
                        "Erro ao adicionar coluna idioma_padrao na tabela USUARIO"
                );
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao verificar coluna idioma_padrao na tabela USUARIO", ex);
        }
    }

    private String normalizarIdiomaPadrao(String idiomaPadrao) {
        if (idiomaPadrao == null || idiomaPadrao.isBlank()) {
            return DEFAULT_LANGUAGE;
        }
        return idiomaPadrao.trim();
    }
}
