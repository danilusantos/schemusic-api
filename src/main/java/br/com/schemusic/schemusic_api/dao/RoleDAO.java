package br.com.schemusic.schemusic_api.dao;

import br.com.schemusic.schemusic_api.bean.RoleBean;
import br.com.schemusic.schemusic_api.bean.UsuarioBean;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class RoleDAO {

    private final DataSource dataSource;

    public RoleDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void criarTabelaSeNaoExistir() {
        String sql = "CREATE TABLE IF NOT EXISTS ROLE_SISTEMA ("
                + "id_role BIGINT AUTO_INCREMENT PRIMARY KEY,"
                + "nome VARCHAR(60) NOT NULL UNIQUE,"
                + "descricao VARCHAR(255),"
                + "ativo BOOLEAN DEFAULT TRUE,"
                + "data_cadastro DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";
        executarDDL(sql, "Erro ao criar tabela ROLE_SISTEMA");
    }

    public Long inserir(RoleBean role) {
        String sql = "INSERT INTO ROLE_SISTEMA (nome, descricao, ativo) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, role.getNome());
            statement.setString(2, role.getDescricao());
            statement.setBoolean(3, Boolean.TRUE.equals(role.getAtivo()));
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
            throw new IllegalStateException("Nao foi possivel obter id da role");
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao inserir role", ex);
        }
    }

    public void atualizar(RoleBean role) {
        String sql = "UPDATE ROLE_SISTEMA SET nome = ?, descricao = ?, ativo = ? WHERE id_role = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, role.getNome());
            statement.setString(2, role.getDescricao());
            statement.setBoolean(3, Boolean.TRUE.equals(role.getAtivo()));
            statement.setLong(4, role.getIdRole());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao atualizar role", ex);
        }
    }

    public void excluir(Long idRole) {
        String sql = "DELETE FROM ROLE_SISTEMA WHERE id_role = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idRole);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao excluir role", ex);
        }
    }

    public RoleBean buscarPorId(Long idRole) {
        String sql = "SELECT id_role, nome, descricao, ativo FROM ROLE_SISTEMA WHERE id_role = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idRole);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapearRole(rs);
                }
            }
            return null;
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao buscar role por id", ex);
        }
    }

    public RoleBean buscarPorNome(String nome) {
        String sql = "SELECT id_role, nome, descricao, ativo FROM ROLE_SISTEMA WHERE nome = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nome);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapearRole(rs);
                }
            }
            return null;
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao buscar role por nome", ex);
        }
    }

    public List<RoleBean> listar() {
        String sql = "SELECT id_role, nome, descricao, ativo FROM ROLE_SISTEMA ORDER BY id_role DESC";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            List<RoleBean> roles = new ArrayList<>();
            while (rs.next()) {
                roles.add(mapearRole(rs));
            }
            return roles;
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao listar roles", ex);
        }
    }

    public long contar(Boolean ativo) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ROLE_SISTEMA WHERE 1=1 ");
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
            throw new IllegalStateException("Erro ao contar roles", ex);
        }
    }

    public List<UsuarioBean> listarUsuariosDaRole(Long idRole) {
        String sql = "SELECT u.id_usuario, u.nome, u.email, u.ativo "
                + "FROM USUARIO_ROLE ur "
                + "INNER JOIN USUARIO u ON u.id_usuario = ur.id_usuario "
                + "WHERE ur.id_role = ? ORDER BY u.id_usuario DESC";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idRole);
            try (ResultSet rs = statement.executeQuery()) {
                List<UsuarioBean> usuarios = new ArrayList<>();
                while (rs.next()) {
                    UsuarioBean usuario = new UsuarioBean();
                    usuario.setIdUsuario(rs.getLong("id_usuario"));
                    usuario.setNome(rs.getString("nome"));
                    usuario.setEmail(rs.getString("email"));
                    usuario.setAtivo(rs.getBoolean("ativo"));
                    usuarios.add(usuario);
                }
                return usuarios;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao listar usuarios da role", ex);
        }
    }

    public void criarRoleSeNaoExistir(String nome, String descricao) {
        if (buscarPorNome(nome) != null) {
            return;
        }
        RoleBean role = new RoleBean();
        role.setNome(nome);
        role.setDescricao(descricao);
        role.setAtivo(true);
        inserir(role);
    }

    private RoleBean mapearRole(ResultSet rs) throws SQLException {
        RoleBean role = new RoleBean();
        role.setIdRole(rs.getLong("id_role"));
        role.setNome(rs.getString("nome"));
        role.setDescricao(rs.getString("descricao"));
        role.setAtivo(rs.getBoolean("ativo"));
        return role;
    }

    private void executarDDL(String sql, String mensagemErro) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        } catch (SQLException ex) {
            throw new IllegalStateException(mensagemErro, ex);
        }
    }
}
