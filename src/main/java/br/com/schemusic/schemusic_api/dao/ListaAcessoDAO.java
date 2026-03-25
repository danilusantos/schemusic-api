package br.com.schemusic.schemusic_api.dao;

import br.com.schemusic.schemusic_api.bean.ListaAcessoBean;
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
public class ListaAcessoDAO {

    private final DataSource dataSource;

    public ListaAcessoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void criarTabelaSeNaoExistir() {
        String sql = "CREATE TABLE IF NOT EXISTS LISTA_ACESSO ("
                + "id_lista_acesso BIGINT AUTO_INCREMENT PRIMARY KEY,"
                + "tipo_lista VARCHAR(20) NOT NULL,"
                + "tipo_alvo VARCHAR(30) NOT NULL,"
                + "valor_alvo VARCHAR(255) NOT NULL,"
                + "observacao VARCHAR(255),"
                + "ativo BOOLEAN DEFAULT TRUE,"
                + "data_cadastro DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";

        executarDDL(sql, "Erro ao criar tabela LISTA_ACESSO");
    }

    public Long inserir(ListaAcessoBean item) {
        String sql = "INSERT INTO LISTA_ACESSO (tipo_lista, tipo_alvo, valor_alvo, observacao, ativo) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, item.getTipoLista());
            statement.setString(2, item.getTipoAlvo());
            statement.setString(3, item.getValorAlvo());
            statement.setString(4, item.getObservacao());
            statement.setBoolean(5, Boolean.TRUE.equals(item.getAtivo()));
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
            throw new IllegalStateException("Nao foi possivel obter id da lista de acesso");
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao inserir item de lista de acesso", ex);
        }
    }

    public void atualizar(ListaAcessoBean item) {
        String sql = "UPDATE LISTA_ACESSO SET tipo_lista = ?, tipo_alvo = ?, valor_alvo = ?, observacao = ?, ativo = ? "
                + "WHERE id_lista_acesso = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, item.getTipoLista());
            statement.setString(2, item.getTipoAlvo());
            statement.setString(3, item.getValorAlvo());
            statement.setString(4, item.getObservacao());
            statement.setBoolean(5, Boolean.TRUE.equals(item.getAtivo()));
            statement.setLong(6, item.getIdListaAcesso());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao atualizar item de lista de acesso", ex);
        }
    }

    public void inativar(Long idListaAcesso) {
        String sql = "UPDATE LISTA_ACESSO SET ativo = FALSE WHERE id_lista_acesso = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idListaAcesso);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao inativar item de lista de acesso", ex);
        }
    }

    public List<ListaAcessoBean> listar(String tipoLista, Boolean ativo) {
        StringBuilder sql = new StringBuilder("SELECT id_lista_acesso, tipo_lista, tipo_alvo, valor_alvo, observacao, ativo "
                + "FROM LISTA_ACESSO WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (tipoLista != null && !tipoLista.isBlank()) {
            sql.append("AND tipo_lista = ? ");
            params.add(tipoLista);
        }
        if (ativo != null) {
            sql.append("AND ativo = ? ");
            params.add(ativo);
        }
        sql.append("ORDER BY id_lista_acesso DESC");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }

            List<ListaAcessoBean> itens = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ListaAcessoBean item = new ListaAcessoBean();
                    item.setIdListaAcesso(rs.getLong("id_lista_acesso"));
                    item.setTipoLista(rs.getString("tipo_lista"));
                    item.setTipoAlvo(rs.getString("tipo_alvo"));
                    item.setValorAlvo(rs.getString("valor_alvo"));
                    item.setObservacao(rs.getString("observacao"));
                    item.setAtivo(rs.getBoolean("ativo"));
                    itens.add(item);
                }
            }
            return itens;
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao listar itens de lista de acesso", ex);
        }
    }

    public long contar(Boolean ativo) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM LISTA_ACESSO WHERE 1=1 ");
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
            throw new IllegalStateException("Erro ao contar itens de lista de acesso", ex);
        }
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
