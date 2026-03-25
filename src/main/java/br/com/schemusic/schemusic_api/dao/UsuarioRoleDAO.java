package br.com.schemusic.schemusic_api.dao;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UsuarioRoleDAO {

    private final DataSource dataSource;

    public UsuarioRoleDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void criarTabelaSeNaoExistir() {
        String sql = "CREATE TABLE IF NOT EXISTS USUARIO_ROLE ("
                + "id_usuario BIGINT NOT NULL,"
                + "id_role BIGINT NOT NULL,"
                + "data_vinculo DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "PRIMARY KEY (id_usuario, id_role),"
                + "CONSTRAINT fk_usuario_role_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO(id_usuario),"
                + "CONSTRAINT fk_usuario_role_role FOREIGN KEY (id_role) REFERENCES ROLE_SISTEMA(id_role)"
                + ")";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao criar tabela USUARIO_ROLE", ex);
        }
    }

    public void vincularSeNaoExistir(Long idUsuario, Long idRole) {
        String sql = "INSERT IGNORE INTO USUARIO_ROLE (id_usuario, id_role) VALUES (?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idUsuario);
            statement.setLong(2, idRole);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao vincular role ao usuario", ex);
        }
    }

    public void substituirRolesDoUsuario(Long idUsuario, List<Long> roleIds) {
        String deleteSql = "DELETE FROM USUARIO_ROLE WHERE id_usuario = ?";
        String insertSql = "INSERT INTO USUARIO_ROLE (id_usuario, id_role) VALUES (?, ?)";

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            try (PreparedStatement delete = connection.prepareStatement(deleteSql)) {
                delete.setLong(1, idUsuario);
                delete.executeUpdate();
            }
            try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
                for (Long roleId : roleIds) {
                    insert.setLong(1, idUsuario);
                    insert.setLong(2, roleId);
                    insert.addBatch();
                }
                insert.executeBatch();
            }
            connection.commit();
        } catch (SQLException ex) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ignored) {
                    // Rollback failure is secondary; original exception is preserved.
                }
            }
            throw new IllegalStateException("Erro ao substituir roles do usuario", ex);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException ignored) {
                    // Connection cleanup failure should not mask the main flow.
                }
            }
        }
    }

    public boolean usuarioPossuiRole(Long idUsuario, String roleNome) {
        String sql = "SELECT 1 FROM USUARIO_ROLE ur "
                + "INNER JOIN ROLE_SISTEMA r ON r.id_role = ur.id_role "
                + "WHERE ur.id_usuario = ? AND r.nome = ? AND r.ativo = TRUE LIMIT 1";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idUsuario);
            statement.setString(2, roleNome);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao verificar role do usuario", ex);
        }
    }

    public List<String> listarNomesRolesDoUsuario(Long idUsuario) {
        String sql = "SELECT r.nome FROM USUARIO_ROLE ur "
                + "INNER JOIN ROLE_SISTEMA r ON r.id_role = ur.id_role "
                + "WHERE ur.id_usuario = ? ORDER BY r.nome";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idUsuario);
            try (ResultSet rs = statement.executeQuery()) {
                List<String> roles = new ArrayList<>();
                while (rs.next()) {
                    roles.add(rs.getString("nome"));
                }
                return roles;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao listar roles do usuario", ex);
        }
    }

    public boolean existeVinculoComRole(Long idRole) {
        String sql = "SELECT 1 FROM USUARIO_ROLE WHERE id_role = ? LIMIT 1";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idRole);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao verificar vinculo de role", ex);
        }
    }
}
