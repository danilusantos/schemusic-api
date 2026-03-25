package br.com.schemusic.schemusic_api.business;

import br.com.schemusic.schemusic_api.bean.AcessoSiteLogBean;
import br.com.schemusic.schemusic_api.dao.AcessoSiteLogDAO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminAcessoLogBusiness {

    private final AcessoSiteLogDAO acessoSiteLogDAO;

    public AdminAcessoLogBusiness(AcessoSiteLogDAO acessoSiteLogDAO) {
        this.acessoSiteLogDAO = acessoSiteLogDAO;
    }

    public List<AcessoSiteLogBean> listar(Integer limite) {
        int limiteFinal = limite == null ? 200 : Math.max(1, Math.min(limite, 1000));
        return acessoSiteLogDAO.listarRecentes(limiteFinal);
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
}
