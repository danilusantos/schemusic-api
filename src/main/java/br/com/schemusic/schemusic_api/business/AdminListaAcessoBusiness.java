package br.com.schemusic.schemusic_api.business;

import br.com.schemusic.schemusic_api.bean.ListaAcessoBean;
import br.com.schemusic.schemusic_api.dao.ListaAcessoDAO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminListaAcessoBusiness {

    private final ListaAcessoDAO listaAcessoDAO;

    public AdminListaAcessoBusiness(ListaAcessoDAO listaAcessoDAO) {
        this.listaAcessoDAO = listaAcessoDAO;
    }

    public ListaAcessoBean criar(String tipoLista, String tipoAlvo, String valorAlvo, String observacao, Boolean ativo) {
        validar(tipoLista, tipoAlvo, valorAlvo);
        ListaAcessoBean item = new ListaAcessoBean();
        item.setTipoLista(tipoLista.trim().toUpperCase());
        item.setTipoAlvo(tipoAlvo.trim().toUpperCase());
        item.setValorAlvo(valorAlvo.trim());
        item.setObservacao(observacao);
        item.setAtivo(ativo == null || ativo);
        Long id = listaAcessoDAO.inserir(item);
        return listaAcessoDAO.listar(null, null).stream().filter(i -> i.getIdListaAcesso().equals(id)).findFirst().orElse(item);
    }

    public ListaAcessoBean editar(Long id, String tipoLista, String tipoAlvo, String valorAlvo, String observacao, Boolean ativo) {
        validar(tipoLista, tipoAlvo, valorAlvo);
        ListaAcessoBean existente = listaAcessoDAO.listar(null, null)
                .stream()
                .filter(item -> item.getIdListaAcesso().equals(id))
                .findFirst()
                .orElse(null);

        if (existente == null) {
            throw new IllegalArgumentException("Item de lista nao encontrado");
        }

        existente.setTipoLista(tipoLista.trim().toUpperCase());
        existente.setTipoAlvo(tipoAlvo.trim().toUpperCase());
        existente.setValorAlvo(valorAlvo.trim());
        existente.setObservacao(observacao);
        existente.setAtivo(ativo != null ? ativo : existente.getAtivo());
        listaAcessoDAO.atualizar(existente);
        return existente;
    }

    public void inativar(Long id) {
        listaAcessoDAO.inativar(id);
    }

    public List<ListaAcessoBean> listar(String tipoLista, Boolean ativo) {
        return listaAcessoDAO.listar(tipoLista != null ? tipoLista.toUpperCase() : null, ativo);
    }

    private void validar(String tipoLista, String tipoAlvo, String valorAlvo) {
        if (tipoLista == null || tipoLista.isBlank()) {
            throw new IllegalArgumentException("tipoLista obrigatorio");
        }
        if (tipoAlvo == null || tipoAlvo.isBlank()) {
            throw new IllegalArgumentException("tipoAlvo obrigatorio");
        }
        if (valorAlvo == null || valorAlvo.isBlank()) {
            throw new IllegalArgumentException("valorAlvo obrigatorio");
        }
    }
}
