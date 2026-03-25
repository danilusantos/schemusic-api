package br.com.schemusic.schemusic_api.dao;

import br.com.schemusic.schemusic_api.bean.AcessoSiteLogBean;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AcessoSiteLogDAO {

    private final DataSource dataSource;

    public AcessoSiteLogDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void criarTabelaSeNaoExistir() {
        String sql = "CREATE TABLE IF NOT EXISTS ACESSO_SITE_LOG ("
                + "id_acesso BIGINT AUTO_INCREMENT PRIMARY KEY,"
                + "metodo VARCHAR(10) NOT NULL,"
                + "caminho VARCHAR(255) NOT NULL,"
                + "ip VARCHAR(45),"
                + "user_id VARCHAR(50),"
                + "status_http INT,"
                + "data_acesso DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao criar tabela ACESSO_SITE_LOG", ex);
        }
    }

    public void inserir(AcessoSiteLogBean log) {
        String sql = "INSERT INTO ACESSO_SITE_LOG (metodo, caminho, ip, user_id, status_http) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, log.getMetodo());
            statement.setString(2, log.getCaminho());
            statement.setString(3, log.getIp());
            statement.setString(4, log.getUserId());
            statement.setInt(5, log.getStatusHttp());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao inserir log de acesso", ex);
        }
    }

    public List<AcessoSiteLogBean> listarRecentes(int limite) {
        String sql = "SELECT id_acesso, metodo, caminho, ip, user_id, status_http, data_acesso "
                + "FROM ACESSO_SITE_LOG ORDER BY id_acesso DESC LIMIT ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, limite);
            List<AcessoSiteLogBean> logs = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    AcessoSiteLogBean log = new AcessoSiteLogBean();
                    log.setIdAcesso(rs.getLong("id_acesso"));
                    log.setMetodo(rs.getString("metodo"));
                    log.setCaminho(rs.getString("caminho"));
                    log.setIp(rs.getString("ip"));
                    log.setUserId(rs.getString("user_id"));
                    log.setStatusHttp(rs.getInt("status_http"));
                    Timestamp dataAcesso = rs.getTimestamp("data_acesso");
                    if (dataAcesso != null) {
                        log.setDataAcesso(dataAcesso.toLocalDateTime());
                    }
                    logs.add(log);
                }
            }
            return logs;
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao listar logs de acesso", ex);
        }
    }

    public long contarTotal() {
        String sql = "SELECT COUNT(*) FROM ACESSO_SITE_LOG";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0L;
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao contar logs de acesso", ex);
        }
    }

    public long contarUltimasHoras(int horas) {
        String sql = "SELECT COUNT(*) FROM ACESSO_SITE_LOG WHERE data_acesso >= DATE_SUB(NOW(), INTERVAL ? HOUR)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, horas);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return 0L;
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao contar logs por janela de tempo", ex);
        }
    }
}
