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
public class RolePermissaoDAO {

    private final DataSource dataSource;

    public RolePermissaoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void criarTabelaSeNaoExistir() {
        String sql = "CREATE TABLE IF NOT EXISTS ROLE_PERMISSAO ("
                + "id_role BIGINT NOT NULL,"
                + "id_permissao BIGINT NOT NULL,"
                + "data_vinculo DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "PRIMARY KEY (id_role, id_permissao),"
                + "CONSTRAINT fk_role_permissao_role FOREIGN KEY (id_role) REFERENCES ROLE_SISTEMA(id_role),"
                + "CONSTRAINT fk_role_permissao_permissao FOREIGN KEY (id_permissao) REFERENCES PERMISSAO_TELA(id_permissao)"
                + ")";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao criar tabela ROLE_PERMISSAO", ex);
        }
    }

    public void substituirPermissoesDaRole(Long idRole, List<Long> idsPermissao) {
        String deleteSql = "DELETE FROM ROLE_PERMISSAO WHERE id_role = ?";
        String insertSql = "INSERT INTO ROLE_PERMISSAO (id_role, id_permissao) VALUES (?, ?)";

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement delete = connection.prepareStatement(deleteSql)) {
                delete.setLong(1, idRole);
                delete.executeUpdate();
            }

            try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
                for (Long idPermissao : idsPermissao) {
                    insert.setLong(1, idRole);
                    insert.setLong(2, idPermissao);
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
                    // rollback best effort
                }
            }
            throw new IllegalStateException("Erro ao substituir permissoes da role", ex);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException ignored) {
                    // cleanup best effort
                }
            }
        }
    }

    public List<Long> listarIdsPermissaoDaRole(Long idRole) {
        String sql = "SELECT id_permissao FROM ROLE_PERMISSAO WHERE id_role = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idRole);
            List<Long> ids = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getLong("id_permissao"));
                }
            }
            return ids;
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao listar permissoes da role", ex);
        }
    }

    public List<Long> listarIdsPermissaoPorUsuario(Long idUsuario) {
        String sql = "SELECT DISTINCT rp.id_permissao FROM ROLE_PERMISSAO rp "
                + "INNER JOIN USUARIO_ROLE ur ON ur.id_role = rp.id_role "
                + "INNER JOIN ROLE_SISTEMA r ON r.id_role = rp.id_role "
                + "WHERE ur.id_usuario = ? AND r.ativo = TRUE";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idUsuario);
            List<Long> ids = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getLong("id_permissao"));
                }
            }
            return ids;
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao listar permissoes por usuario via roles", ex);
        }
    }

    public void excluirPermissoes(List<Long> idsPermissao) {
        if (idsPermissao == null || idsPermissao.isEmpty()) {
            return;
        }

        String sql = "DELETE FROM ROLE_PERMISSAO WHERE id_permissao IN (" + 
                     String.join(",", idsPermissao.stream().map(id -> "?").toList()) + ")";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < idsPermissao.size(); i++) {
                statement.setLong(i + 1, idsPermissao.get(i));
            }
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao excluir vinculos de permissoes", ex);
        }
    }
}
