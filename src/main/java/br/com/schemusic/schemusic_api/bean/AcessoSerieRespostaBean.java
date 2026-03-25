package br.com.schemusic.schemusic_api.bean;

import java.util.List;

public class AcessoSerieRespostaBean {

    private String periodo;
    private List<AcessoSeriePontoBean> pontos;

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public List<AcessoSeriePontoBean> getPontos() {
        return pontos;
    }

    public void setPontos(List<AcessoSeriePontoBean> pontos) {
        this.pontos = pontos;
    }
}
