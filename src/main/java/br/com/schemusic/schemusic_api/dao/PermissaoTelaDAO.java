package br.com.schemusic.schemusic_api.dao;

import br.com.schemusic.schemusic_api.bean.PermissaoTelaBean;
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
public class PermissaoTelaDAO {

    private final DataSource dataSource;

    public PermissaoTelaDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void criarTabelaSeNaoExistir() {
        String sql = "CREATE TABLE IF NOT EXISTS PERMISSAO_TELA ("
                + "id_permissao BIGINT AUTO_INCREMENT PRIMARY KEY,"
                + "id_grupo BIGINT NULL,"
                + "tela_codigo VARCHAR(80) NOT NULL,"
                + "acao_codigo VARCHAR(30) NOT NULL,"
                + "descricao VARCHAR(255),"
                + "ativo BOOLEAN DEFAULT TRUE,"
                + "data_cadastro DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "UNIQUE KEY uk_tela_acao (tela_codigo, acao_codigo)"
                + ")";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
            adicionarColunaGrupoSeNecessario(connection);
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao criar tabela PERMISSAO_TELA", ex);
        }
    }

    public Long inserir(PermissaoTelaBean permissaoTela) {
        String sql = "INSERT INTO PERMISSAO_TELA (id_grupo, tela_codigo, acao_codigo, descricao, ativo) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (permissaoTela.getIdGrupo() == null) {
                statement.setNull(1, java.sql.Types.BIGINT);
            } else {
                statement.setLong(1, permissaoTela.getIdGrupo());
            }
            statement.setString(2, permissaoTela.getTelaCodigo());
            statement.setString(3, permissaoTela.getAcaoCodigo());
            statement.setString(4, permissaoTela.getDescricao());
            statement.setBoolean(5, permissaoTela.getAtivo() == null || permissaoTela.getAtivo());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
            throw new IllegalStateException("Nao foi possivel obter id da permissao");
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao inserir permissao", ex);
        }
    }

    public PermissaoTelaBean buscarPorTelaEAcao(String telaCodigo, String acaoCodigo) {
        String sql = "SELECT pt.id_permissao, pt.id_grupo, pt.tela_codigo, pt.acao_codigo, pt.descricao, pt.ativo, pg.codigo_grupo, pg.nome_grupo "
            + "FROM PERMISSAO_TELA pt LEFT JOIN PERMISSAO_GRUPO pg ON pg.id_grupo = pt.id_grupo "
            + "WHERE pt.tela_codigo = ? AND pt.acao_codigo = ? LIMIT 1";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, telaCodigo);
            statement.setString(2, acaoCodigo);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
            return null;
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao buscar permissao por tela/acao", ex);
        }
    }

    public PermissaoTelaBean buscarPorId(Long idPermissao) {
        String sql = "SELECT pt.id_permissao, pt.id_grupo, pt.tela_codigo, pt.acao_codigo, pt.descricao, pt.ativo, pg.codigo_grupo, pg.nome_grupo "
            + "FROM PERMISSAO_TELA pt LEFT JOIN PERMISSAO_GRUPO pg ON pg.id_grupo = pt.id_grupo WHERE pt.id_permissao = ? LIMIT 1";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idPermissao);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
            return null;
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao buscar permissao por id", ex);
        }
    }

    public List<PermissaoTelaBean> listarAtivas() {
        String sql = "SELECT pt.id_permissao, pt.id_grupo, pt.tela_codigo, pt.acao_codigo, pt.descricao, pt.ativo, pg.codigo_grupo, pg.nome_grupo "
            + "FROM PERMISSAO_TELA pt LEFT JOIN PERMISSAO_GRUPO pg ON pg.id_grupo = pt.id_grupo "
            + "WHERE pt.ativo = TRUE ORDER BY COALESCE(pg.nome_grupo, pt.tela_codigo), pt.tela_codigo, pt.acao_codigo";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            List<PermissaoTelaBean> permissoes = new ArrayList<>();
            while (rs.next()) {
                permissoes.add(mapear(rs));
            }
            return permissoes;
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao listar permissoes", ex);
        }
    }

    public List<PermissaoTelaBean> listarPorTela(String telaCodigo) {
        String sql = "SELECT pt.id_permissao, pt.id_grupo, pt.tela_codigo, pt.acao_codigo, pt.descricao, pt.ativo, pg.codigo_grupo, pg.nome_grupo "
            + "FROM PERMISSAO_TELA pt LEFT JOIN PERMISSAO_GRUPO pg ON pg.id_grupo = pt.id_grupo "
            + "WHERE pt.tela_codigo = ? AND pt.ativo = TRUE ORDER BY pt.acao_codigo";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, telaCodigo);
            List<PermissaoTelaBean> permissoes = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    permissoes.add(mapear(rs));
                }
            }
            return permissoes;
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao listar permissoes por tela", ex);
        }
    }

    private PermissaoTelaBean mapear(ResultSet rs) throws SQLException {
        PermissaoTelaBean permissao = new PermissaoTelaBean();
        permissao.setIdPermissao(rs.getLong("id_permissao"));
        long idGrupo = rs.getLong("id_grupo");
        if (!rs.wasNull()) {
            permissao.setIdGrupo(idGrupo);
        }
        permissao.setGrupoCodigo(rs.getString("codigo_grupo"));
        permissao.setGrupoNome(rs.getString("nome_grupo"));
        permissao.setTelaCodigo(rs.getString("tela_codigo"));
        permissao.setAcaoCodigo(rs.getString("acao_codigo"));
        permissao.setDescricao(rs.getString("descricao"));
        permissao.setAtivo(rs.getBoolean("ativo"));
        return permissao;
    }

    public void atualizarGrupoDaPermissao(Long idPermissao, Long idGrupo) {
        String sql = "UPDATE PERMISSAO_TELA SET id_grupo = ? WHERE id_permissao = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (idGrupo == null) {
                statement.setNull(1, java.sql.Types.BIGINT);
            } else {
                statement.setLong(1, idGrupo);
            }
            statement.setLong(2, idPermissao);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao atualizar grupo da permissao", ex);
        }
    }

    public void excluir(Long idPermissao) {
        String sql = "DELETE FROM PERMISSAO_TELA WHERE id_permissao = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idPermissao);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao excluir permissao", ex);
        }
    }

    public void excluirMultiplos(List<Long> idsPermissao) {
        if (idsPermissao == null || idsPermissao.isEmpty()) {
            return;
        }

        String sql = "DELETE FROM PERMISSAO_TELA WHERE id_permissao IN (" + 
                     String.join(",", idsPermissao.stream().map(id -> "?").toList()) + ")";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < idsPermissao.size(); i++) {
                statement.setLong(i + 1, idsPermissao.get(i));
            }
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao excluir permissoes", ex);
        }
    }

    private void adicionarColunaGrupoSeNecessario(Connection connection) throws SQLException {
        boolean colunaExiste = false;
        try (ResultSet columns = connection.getMetaData().getColumns(null, null, "PERMISSAO_TELA", "id_grupo")) {
            colunaExiste = columns.next();
        }

        if (!colunaExiste) {
            try (PreparedStatement alter = connection.prepareStatement("ALTER TABLE PERMISSAO_TELA ADD COLUMN id_grupo BIGINT NULL AFTER id_permissao")) {
                alter.execute();
            }
        }
    }
}
