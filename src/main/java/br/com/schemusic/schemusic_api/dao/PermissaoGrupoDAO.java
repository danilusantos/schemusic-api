package br.com.schemusic.schemusic_api.dao;

import br.com.schemusic.schemusic_api.bean.PermissaoGrupoBean;
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
public class PermissaoGrupoDAO {

    private final DataSource dataSource;

    public PermissaoGrupoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void criarTabelaSeNaoExistir() {
        String sql = "CREATE TABLE IF NOT EXISTS PERMISSAO_GRUPO ("
                + "id_grupo BIGINT AUTO_INCREMENT PRIMARY KEY,"
                + "codigo_grupo VARCHAR(80) NOT NULL UNIQUE,"
                + "nome_grupo VARCHAR(150) NOT NULL,"
                + "descricao VARCHAR(255),"
                + "ativo BOOLEAN DEFAULT TRUE,"
                + "data_cadastro DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao criar tabela PERMISSAO_GRUPO", ex);
        }
    }

    public Long inserir(PermissaoGrupoBean grupo) {
        String sql = "INSERT INTO PERMISSAO_GRUPO (codigo_grupo, nome_grupo, descricao, ativo) VALUES (?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, grupo.getCodigoGrupo());
            statement.setString(2, grupo.getNomeGrupo());
            statement.setString(3, grupo.getDescricao());
            statement.setBoolean(4, grupo.getAtivo() == null || grupo.getAtivo());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
            throw new IllegalStateException("Nao foi possivel obter id do grupo");
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao inserir grupo", ex);
        }
    }

    public PermissaoGrupoBean buscarPorId(Long idGrupo) {
        String sql = "SELECT id_grupo, codigo_grupo, nome_grupo, descricao, ativo FROM PERMISSAO_GRUPO WHERE id_grupo = ? LIMIT 1";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idGrupo);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
            return null;
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao buscar grupo por id", ex);
        }
    }

    public PermissaoGrupoBean buscarPorCodigo(String codigoGrupo) {
        String sql = "SELECT id_grupo, codigo_grupo, nome_grupo, descricao, ativo FROM PERMISSAO_GRUPO WHERE codigo_grupo = ? LIMIT 1";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, codigoGrupo);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
            return null;
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao buscar grupo por codigo", ex);
        }
    }

    public List<PermissaoGrupoBean> listarAtivos() {
        String sql = "SELECT id_grupo, codigo_grupo, nome_grupo, descricao, ativo FROM PERMISSAO_GRUPO WHERE ativo = TRUE ORDER BY nome_grupo";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<PermissaoGrupoBean> grupos = new ArrayList<>();
            while (rs.next()) {
                grupos.add(mapear(rs));
            }
            return grupos;
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao listar grupos", ex);
        }
    }

    public List<PermissaoGrupoBean> listarTodos() {
        String sql = "SELECT id_grupo, codigo_grupo, nome_grupo, descricao, ativo FROM PERMISSAO_GRUPO ORDER BY nome_grupo";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<PermissaoGrupoBean> grupos = new ArrayList<>();
            while (rs.next()) {
                grupos.add(mapear(rs));
            }
            return grupos;
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao listar grupos", ex);
        }
    }

    public void atualizar(PermissaoGrupoBean grupo) {
        String sql = "UPDATE PERMISSAO_GRUPO SET codigo_grupo = ?, nome_grupo = ?, descricao = ?, ativo = ? WHERE id_grupo = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, grupo.getCodigoGrupo());
            statement.setString(2, grupo.getNomeGrupo());
            statement.setString(3, grupo.getDescricao());
            statement.setBoolean(4, Boolean.TRUE.equals(grupo.getAtivo()));
            statement.setLong(5, grupo.getIdGrupo());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao atualizar grupo", ex);
        }
    }

    public void excluir(Long idGrupo) {
        String sql = "DELETE FROM PERMISSAO_GRUPO WHERE id_grupo = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idGrupo);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao excluir grupo", ex);
        }
    }

    public void excluirMultiplos(List<Long> idsGrupo) {
        if (idsGrupo == null || idsGrupo.isEmpty()) {
            return;
        }

        String sql = "DELETE FROM PERMISSAO_GRUPO WHERE id_grupo IN (" + 
                     String.join(",", idsGrupo.stream().map(id -> "?").toList()) + ")";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < idsGrupo.size(); i++) {
                statement.setLong(i + 1, idsGrupo.get(i));
            }
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao excluir grupos", ex);
        }
    }

    private PermissaoGrupoBean mapear(ResultSet rs) throws SQLException {
        PermissaoGrupoBean grupo = new PermissaoGrupoBean();
        grupo.setIdGrupo(rs.getLong("id_grupo"));
        grupo.setCodigoGrupo(rs.getString("codigo_grupo"));
        grupo.setNomeGrupo(rs.getString("nome_grupo"));
        grupo.setDescricao(rs.getString("descricao"));
        grupo.setAtivo(rs.getBoolean("ativo"));
        return grupo;
    }
}