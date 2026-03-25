package br.com.schemusic.schemusic_api.bean;

public class ListaAcessoBean {

    private Long idListaAcesso;
    private String tipoLista;
    private String tipoAlvo;
    private String valorAlvo;
    private String observacao;
    private Boolean ativo;

    public Long getIdListaAcesso() {
        return idListaAcesso;
    }

    public void setIdListaAcesso(Long idListaAcesso) {
        this.idListaAcesso = idListaAcesso;
    }

    public String getTipoLista() {
        return tipoLista;
    }

    public void setTipoLista(String tipoLista) {
        this.tipoLista = tipoLista;
    }

    public String getTipoAlvo() {
        return tipoAlvo;
    }

    public void setTipoAlvo(String tipoAlvo) {
        this.tipoAlvo = tipoAlvo;
    }

    public String getValorAlvo() {
        return valorAlvo;
    }

    public void setValorAlvo(String valorAlvo) {
        this.valorAlvo = valorAlvo;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
