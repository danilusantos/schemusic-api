package br.com.schemusic.schemusic_api.delegate;

import br.com.schemusic.schemusic_api.business.AdminAcessoLogBusiness;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class AdministrationAccessLogDelegate {

    private final AdminAcessoLogBusiness adminAcessoLogBusiness;

    public AdministrationAccessLogDelegate(AdminAcessoLogBusiness adminAcessoLogBusiness) {
        this.adminAcessoLogBusiness = adminAcessoLogBusiness;
    }

    public ResponseEntity<?> listar(Integer limite) {
        return ResponseEntity.ok(adminAcessoLogBusiness.listar(limite));
    }

    public void registrarNavegacaoFrontend(
            String rota,
            String origem,
            String userAgent,
            String ip,
            String userId
    ) {
        adminAcessoLogBusiness.registrarNavegacaoFrontend(rota, origem, userAgent, ip, userId);
    }
}
