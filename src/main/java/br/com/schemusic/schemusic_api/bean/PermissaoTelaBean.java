package br.com.schemusic.schemusic_api.bean;

public class PermissaoTelaBean {

    private Long idPermissao;
    private Long idGrupo;
    private String grupoCodigo;
    private String grupoNome;
    private String telaCodigo;
    private String acaoCodigo;
    private String descricao;
    private Boolean ativo;

    public Long getIdPermissao() {
        return idPermissao;
    }

    public void setIdPermissao(Long idPermissao) {
        this.idPermissao = idPermissao;
    }

    public Long getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(Long idGrupo) {
        this.idGrupo = idGrupo;
    }

    public String getGrupoCodigo() {
        return grupoCodigo;
    }

    public void setGrupoCodigo(String grupoCodigo) {
        this.grupoCodigo = grupoCodigo;
    }

    public String getGrupoNome() {
        return grupoNome;
    }

    public void setGrupoNome(String grupoNome) {
        this.grupoNome = grupoNome;
    }

    public String getTelaCodigo() {
        return telaCodigo;
    }

    public void setTelaCodigo(String telaCodigo) {
        this.telaCodigo = telaCodigo;
    }

    public String getAcaoCodigo() {
        return acaoCodigo;
    }

    public void setAcaoCodigo(String acaoCodigo) {
        this.acaoCodigo = acaoCodigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
