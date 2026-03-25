package br.com.schemusic.schemusic_api.delegate;

import br.com.schemusic.schemusic_api.business.AdministrationDashboardBusiness;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class AdministrationDashboardDelegate {

    private final AdministrationDashboardBusiness administrationDashboardBusiness;

    public AdministrationDashboardDelegate(AdministrationDashboardBusiness administrationDashboardBusiness) {
        this.administrationDashboardBusiness = administrationDashboardBusiness;
    }

    public ResponseEntity<?> resumo() {
        return ResponseEntity.ok(administrationDashboardBusiness.obterResumo());
    }
}
