package br.com.schemusic.schemusic_api.bean;

public class DashboardResumoBean {

    private Long totalUsuarios;
    private Long totalRoles;
    private Long totalListasAcesso;
    private Long totalLogs;
    private Long totalLogsUltimas24h;
    private Long totalConfigs;

    public Long getTotalUsuarios() {
        return totalUsuarios;
    }

    public void setTotalUsuarios(Long totalUsuarios) {
        this.totalUsuarios = totalUsuarios;
    }

    public Long getTotalRoles() {
        return totalRoles;
    }

    public void setTotalRoles(Long totalRoles) {
        this.totalRoles = totalRoles;
    }

    public Long getTotalListasAcesso() {
        return totalListasAcesso;
    }

    public void setTotalListasAcesso(Long totalListasAcesso) {
        this.totalListasAcesso = totalListasAcesso;
    }

    public Long getTotalLogs() {
        return totalLogs;
    }

    public void setTotalLogs(Long totalLogs) {
        this.totalLogs = totalLogs;
    }

    public Long getTotalLogsUltimas24h() {
        return totalLogsUltimas24h;
    }

    public void setTotalLogsUltimas24h(Long totalLogsUltimas24h) {
        this.totalLogsUltimas24h = totalLogsUltimas24h;
    }

    public Long getTotalConfigs() {
        return totalConfigs;
    }

    public void setTotalConfigs(Long totalConfigs) {
        this.totalConfigs = totalConfigs;
    }
}
