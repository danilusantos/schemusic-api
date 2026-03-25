package br.com.schemusic.schemusic_api.business;

import br.com.schemusic.schemusic_api.bean.DashboardResumoBean;
import br.com.schemusic.schemusic_api.dao.AcessoSiteLogDAO;
import br.com.schemusic.schemusic_api.dao.ListaAcessoDAO;
import br.com.schemusic.schemusic_api.dao.RoleDAO;
import br.com.schemusic.schemusic_api.dao.SistemaConfigDAO;
import br.com.schemusic.schemusic_api.dao.UsuarioDAO;
import org.springframework.stereotype.Service;

@Service
public class AdministrationDashboardBusiness {

    private final UsuarioDAO usuarioDAO;
    private final RoleDAO roleDAO;
    private final ListaAcessoDAO listaAcessoDAO;
    private final AcessoSiteLogDAO acessoSiteLogDAO;
    private final SistemaConfigDAO sistemaConfigDAO;

    public AdministrationDashboardBusiness(UsuarioDAO usuarioDAO,
                                           RoleDAO roleDAO,
                                           ListaAcessoDAO listaAcessoDAO,
                                           AcessoSiteLogDAO acessoSiteLogDAO,
                                           SistemaConfigDAO sistemaConfigDAO) {
        this.usuarioDAO = usuarioDAO;
        this.roleDAO = roleDAO;
        this.listaAcessoDAO = listaAcessoDAO;
        this.acessoSiteLogDAO = acessoSiteLogDAO;
        this.sistemaConfigDAO = sistemaConfigDAO;
    }

    public DashboardResumoBean obterResumo() {
        DashboardResumoBean resumo = new DashboardResumoBean();
        resumo.setTotalUsuarios(usuarioDAO.contar(null));
        resumo.setTotalRoles(roleDAO.contar(null));
        resumo.setTotalListasAcesso(listaAcessoDAO.contar(null));
        resumo.setTotalLogs(acessoSiteLogDAO.contarTotal());
        resumo.setTotalLogsUltimas24h(acessoSiteLogDAO.contarUltimasHoras(24));
        resumo.setTotalConfigs(sistemaConfigDAO.contar(null));
        return resumo;
    }
}
