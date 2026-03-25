package br.com.schemusic.schemusic_api.dao;

import br.com.schemusic.schemusic_api.bean.SistemaConfigBean;
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
public class SistemaConfigDAO {

    private final DataSource dataSource;

    public SistemaConfigDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void criarTabelaSeNaoExistir() {
        String sql = "CREATE TABLE IF NOT EXISTS SISTEMA_CONFIG ("
                + "id_config BIGINT AUTO_INCREMENT PRIMARY KEY,"
                + "chave VARCHAR(120) NOT NULL UNIQUE,"
                + "valor TEXT,"
                + "descricao VARCHAR(255),"
                + "ativo BOOLEAN DEFAULT TRUE,"
                + "data_atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                + ")";

        executarDDL(sql, "Erro ao criar tabela SISTEMA_CONFIG");
    }

    public Long inserir(SistemaConfigBean config) {
        String sql = "INSERT INTO SISTEMA_CONFIG (chave, valor, descricao, ativo) VALUES (?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, config.getChave());
            statement.setString(2, config.getValor());
            statement.setString(3, config.getDescricao());
            statement.setBoolean(4, Boolean.TRUE.equals(config.getAtivo()));
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
            throw new IllegalStateException("Nao foi possivel obter id da config");
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao inserir configuracao", ex);
        }
    }

    public void atualizar(SistemaConfigBean config) {
        String sql = "UPDATE SISTEMA_CONFIG SET chave = ?, valor = ?, descricao = ?, ativo = ? WHERE id_config = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, config.getChave());
            statement.setString(2, config.getValor());
            statement.setString(3, config.getDescricao());
            statement.setBoolean(4, Boolean.TRUE.equals(config.getAtivo()));
            statement.setLong(5, config.getIdConfig());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao atualizar configuracao", ex);
        }
    }

    public void excluir(Long idConfig) {
        String sql = "DELETE FROM SISTEMA_CONFIG WHERE id_config = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idConfig);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao excluir configuracao", ex);
        }
    }

    public SistemaConfigBean buscarPorChave(String chave) {
        String sql = "SELECT id_config, chave, valor, descricao, ativo FROM SISTEMA_CONFIG WHERE chave = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, chave);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
            return null;
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao buscar configuracao por chave", ex);
        }
    }

    public List<SistemaConfigBean> listar() {
        String sql = "SELECT id_config, chave, valor, descricao, ativo FROM SISTEMA_CONFIG ORDER BY id_config DESC";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<SistemaConfigBean> configs = new ArrayList<>();
            while (rs.next()) {
                configs.add(mapear(rs));
            }
            return configs;
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao listar configuracoes", ex);
        }
    }

    public long contar(Boolean ativo) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM SISTEMA_CONFIG WHERE 1=1 ");
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
            throw new IllegalStateException("Erro ao contar configuracoes", ex);
        }
    }

    public void criarOuAtualizarPorChave(String chave, String valor, String descricao) {
        SistemaConfigBean existente = buscarPorChave(chave);
        if (existente == null) {
            SistemaConfigBean config = new SistemaConfigBean();
            config.setChave(chave);
            config.setValor(valor);
            config.setDescricao(descricao);
            config.setAtivo(true);
            inserir(config);
            return;
        }
        existente.setValor(valor);
        existente.setDescricao(descricao);
        existente.setAtivo(true);
        atualizar(existente);
    }

    private SistemaConfigBean mapear(ResultSet rs) throws SQLException {
        SistemaConfigBean config = new SistemaConfigBean();
        config.setIdConfig(rs.getLong("id_config"));
        config.setChave(rs.getString("chave"));
        config.setValor(rs.getString("valor"));
        config.setDescricao(rs.getString("descricao"));
        config.setAtivo(rs.getBoolean("ativo"));
        return config;
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
