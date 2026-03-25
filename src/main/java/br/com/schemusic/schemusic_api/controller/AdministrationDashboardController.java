package br.com.schemusic.schemusic_api.controller;

import br.com.schemusic.schemusic_api.delegate.AdministrationDashboardDelegate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/administration/dashboard")
public class AdministrationDashboardController {

    private final AdministrationDashboardDelegate administrationDashboardDelegate;

    public AdministrationDashboardController(AdministrationDashboardDelegate administrationDashboardDelegate) {
        this.administrationDashboardDelegate = administrationDashboardDelegate;
    }

    @GetMapping("/summary")
    public ResponseEntity<?> resumo() {
        return administrationDashboardDelegate.resumo();
    }
}
