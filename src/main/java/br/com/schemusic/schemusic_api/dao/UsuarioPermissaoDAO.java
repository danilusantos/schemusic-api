package br.com.schemusic.schemusic_api.dao;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UsuarioPermissaoDAO {

    private final DataSource dataSource;

    public UsuarioPermissaoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void criarTabelaSeNaoExistir() {
        String sql = "CREATE TABLE IF NOT EXISTS USUARIO_PERMISSAO ("
                + "id_usuario BIGINT NOT NULL,"
                + "id_permissao BIGINT NOT NULL,"
                + "permitido BOOLEAN NOT NULL DEFAULT TRUE,"
                + "data_vinculo DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "PRIMARY KEY (id_usuario, id_permissao),"
                + "CONSTRAINT fk_usuario_permissao_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO(id_usuario),"
                + "CONSTRAINT fk_usuario_permissao_permissao FOREIGN KEY (id_permissao) REFERENCES PERMISSAO_TELA(id_permissao)"
                + ")";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao criar tabela USUARIO_PERMISSAO", ex);
        }
    }

    public void substituirPermissoesDoUsuario(Long idUsuario, List<Map<String, Object>> overrides) {
        String deleteSql = "DELETE FROM USUARIO_PERMISSAO WHERE id_usuario = ?";
        String insertSql = "INSERT INTO USUARIO_PERMISSAO (id_usuario, id_permissao, permitido) VALUES (?, ?, ?)";

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement delete = connection.prepareStatement(deleteSql)) {
                delete.setLong(1, idUsuario);
                delete.executeUpdate();
            }

            try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
                for (Map<String, Object> item : overrides) {
                    Long idPermissao = (Long) item.get("idPermissao");
                    Boolean permitido = (Boolean) item.get("permitido");
                    insert.setLong(1, idUsuario);
                    insert.setLong(2, idPermissao);
                    insert.setBoolean(3, permitido != null && permitido);
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
            throw new IllegalStateException("Erro ao substituir permissoes do usuario", ex);
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

    public Map<Long, Boolean> listarOverridesDoUsuario(Long idUsuario) {
        String sql = "SELECT id_permissao, permitido FROM USUARIO_PERMISSAO WHERE id_usuario = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idUsuario);
            Map<Long, Boolean> overrides = new HashMap<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    overrides.put(rs.getLong("id_permissao"), rs.getBoolean("permitido"));
                }
            }
            return overrides;
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao listar overrides de permissao do usuario", ex);
        }
    }
}
