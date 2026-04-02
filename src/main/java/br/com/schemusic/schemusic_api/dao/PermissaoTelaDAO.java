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
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao criar tabela PERMISSAO_TELA", ex);
        }
    }

    public Long inserir(PermissaoTelaBean permissaoTela) {
        String sql = "INSERT INTO PERMISSAO_TELA (tela_codigo, acao_codigo, descricao, ativo) VALUES (?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, permissaoTela.getTelaCodigo());
            statement.setString(2, permissaoTela.getAcaoCodigo());
            statement.setString(3, permissaoTela.getDescricao());
            statement.setBoolean(4, permissaoTela.getAtivo() == null || permissaoTela.getAtivo());
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
        String sql = "SELECT id_permissao, tela_codigo, acao_codigo, descricao, ativo FROM PERMISSAO_TELA WHERE tela_codigo = ? AND acao_codigo = ? LIMIT 1";
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
        String sql = "SELECT id_permissao, tela_codigo, acao_codigo, descricao, ativo FROM PERMISSAO_TELA WHERE id_permissao = ? LIMIT 1";
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
        String sql = "SELECT id_permissao, tela_codigo, acao_codigo, descricao, ativo FROM PERMISSAO_TELA WHERE ativo = TRUE ORDER BY tela_codigo, acao_codigo";
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
        String sql = "SELECT id_permissao, tela_codigo, acao_codigo, descricao, ativo FROM PERMISSAO_TELA WHERE tela_codigo = ? AND ativo = TRUE ORDER BY acao_codigo";
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
        permissao.setTelaCodigo(rs.getString("tela_codigo"));
        permissao.setAcaoCodigo(rs.getString("acao_codigo"));
        permissao.setDescricao(rs.getString("descricao"));
        permissao.setAtivo(rs.getBoolean("ativo"));
        return permissao;
    }
}
