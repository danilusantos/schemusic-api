package br.com.schemusic.schemusic_api.business;

import br.com.schemusic.schemusic_api.bean.AcessoSiteLogBean;
import br.com.schemusic.schemusic_api.bean.AcessoSeriePontoBean;
import br.com.schemusic.schemusic_api.bean.AcessoSerieRespostaBean;
import br.com.schemusic.schemusic_api.dao.AcessoSiteLogDAO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AdminAcessoLogBusiness {

    private static final String PERIODO_WEEK = "week";
    private static final String PERIODO_MONTH = "month";
    private static final String PERIODO_YEAR = "year";

    private final AcessoSiteLogDAO acessoSiteLogDAO;

    public AdminAcessoLogBusiness(AcessoSiteLogDAO acessoSiteLogDAO) {
        this.acessoSiteLogDAO = acessoSiteLogDAO;
    }

    public List<AcessoSiteLogBean> listar(Integer limite) {
        int limiteFinal = limite == null ? 200 : Math.max(1, Math.min(limite, 1000));
        return acessoSiteLogDAO.listarRecentes(limiteFinal);
    }

    public AcessoSerieRespostaBean obterSeriePorPeriodo(String periodo) {
        String periodoNormalizado = normalizarPeriodo(periodo);
        AcessoSerieRespostaBean resposta = new AcessoSerieRespostaBean();
        resposta.setPeriodo(periodoNormalizado);

        if (PERIODO_YEAR.equals(periodoNormalizado)) {
            resposta.setPontos(construirSeriePorMes());
            return resposta;
        }

        resposta.setPontos(construirSeriePorDia(periodoNormalizado));
        return resposta;
    }

    public void registrarNavegacaoFrontend(
            String rota,
            String origem,
            String userAgent,
            String ip,
            String userId
    ) {
        if (rota == null || rota.isBlank()) {
            throw new IllegalArgumentException("Rota obrigatoria para registrar navegacao");
        }

        String rotaNormalizada = rota.trim();
        if (rotaNormalizada.length() > 120) {
            rotaNormalizada = rotaNormalizada.substring(0, 120);
        }

        String origemNormalizada = origem == null ? "" : origem.trim();
        if (origemNormalizada.length() > 60) {
            origemNormalizada = origemNormalizada.substring(0, 60);
        }

        String userAgentNormalizado = userAgent == null ? "" : userAgent.trim();
        if (userAgentNormalizado.length() > 60) {
            userAgentNormalizado = userAgentNormalizado.substring(0, 60);
        }

        AcessoSiteLogBean log = new AcessoSiteLogBean();
        log.setMetodo("NAVIGATE");
        log.setCaminho("FRONT:" + rotaNormalizada + "|ORG:" + origemNormalizada + "|UA:" + userAgentNormalizado);
        log.setIp(ip);
        log.setUserId(userId);
        log.setStatusHttp(200);
        acessoSiteLogDAO.inserir(log);
    }

    private List<AcessoSeriePontoBean> construirSeriePorDia(String periodo) {
        int diasParaTras = PERIODO_WEEK.equals(periodo) ? 6 : 29;
        Map<String, Long> totais = acessoSiteLogDAO.contarPorDiaUltimosDias(diasParaTras);
        List<AcessoSeriePontoBean> pontos = new ArrayList<>();
        LocalDate inicio = LocalDate.now().minusDays(diasParaTras);
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("dd/MM");

        for (int i = 0; i <= diasParaTras; i++) {
            LocalDate data = inicio.plusDays(i);
            String referencia = data.toString();
            AcessoSeriePontoBean ponto = new AcessoSeriePontoBean();
            ponto.setReferencia(referencia);
            ponto.setLabel(data.format(labelFormatter));
            ponto.setTotal(totais.getOrDefault(referencia, 0L));
            pontos.add(ponto);
        }

        return pontos;
    }

    private List<AcessoSeriePontoBean> construirSeriePorMes() {
        int quantidadeMeses = 12;
        Map<String, Long> totais = acessoSiteLogDAO.contarPorMesUltimosMeses(quantidadeMeses);
        List<AcessoSeriePontoBean> pontos = new ArrayList<>();
        YearMonth inicio = YearMonth.now().minusMonths(quantidadeMeses - 1L);
        DateTimeFormatter referenciaFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MMM/yy", Locale.US);

        for (int i = 0; i < quantidadeMeses; i++) {
            YearMonth mes = inicio.plusMonths(i);
            String referencia = mes.format(referenciaFormatter);
            AcessoSeriePontoBean ponto = new AcessoSeriePontoBean();
            ponto.setReferencia(referencia);
            ponto.setLabel(mes.format(labelFormatter));
            ponto.setTotal(totais.getOrDefault(referencia, 0L));
            pontos.add(ponto);
        }

        return pontos;
    }

    private String normalizarPeriodo(String periodo) {
        if (periodo == null || periodo.isBlank()) {
            return PERIODO_WEEK;
        }

        String normalizado = periodo.trim().toLowerCase(Locale.ROOT);
        if (PERIODO_WEEK.equals(normalizado) || PERIODO_MONTH.equals(normalizado) || PERIODO_YEAR.equals(normalizado)) {
            return normalizado;
        }

        throw new IllegalArgumentException("Periodo invalido. Use: week, month ou year");
    }
}
