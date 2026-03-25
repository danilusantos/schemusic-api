package br.com.schemusic.schemusic_api.business;

import br.com.schemusic.schemusic_api.bean.SistemaConfigBean;
import br.com.schemusic.schemusic_api.dao.SistemaConfigDAO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminSistemaConfigBusiness {

    private final SistemaConfigDAO sistemaConfigDAO;

    public AdminSistemaConfigBusiness(SistemaConfigDAO sistemaConfigDAO) {
        this.sistemaConfigDAO = sistemaConfigDAO;
    }

    public SistemaConfigBean criar(String chave, String valor, String descricao, Boolean ativo) {
        if (chave == null || chave.isBlank()) {
            throw new IllegalArgumentException("Chave obrigatoria");
        }
        if (sistemaConfigDAO.buscarPorChave(chave.trim()) != null) {
            throw new IllegalArgumentException("Chave ja cadastrada");
        }
        SistemaConfigBean config = new SistemaConfigBean();
        config.setChave(chave.trim());
        config.setValor(valor);
        config.setDescricao(descricao);
        config.setAtivo(ativo == null || ativo);
        Long id = sistemaConfigDAO.inserir(config);
        return sistemaConfigDAO.listar().stream().filter(c -> c.getIdConfig().equals(id)).findFirst().orElse(config);
    }

    public SistemaConfigBean editar(Long idConfig, String chave, String valor, String descricao, Boolean ativo) {
        List<SistemaConfigBean> configs = sistemaConfigDAO.listar();
        SistemaConfigBean existente = configs.stream().filter(c -> c.getIdConfig().equals(idConfig)).findFirst().orElse(null);
        if (existente == null) {
            throw new IllegalArgumentException("Configuracao nao encontrada");
        }
        if (chave == null || chave.isBlank()) {
            throw new IllegalArgumentException("Chave obrigatoria");
        }
        SistemaConfigBean porChave = sistemaConfigDAO.buscarPorChave(chave.trim());
        if (porChave != null && !porChave.getIdConfig().equals(idConfig)) {
            throw new IllegalArgumentException("Chave ja cadastrada");
        }
        existente.setChave(chave.trim());
        existente.setValor(valor);
        existente.setDescricao(descricao);
        existente.setAtivo(ativo != null ? ativo : existente.getAtivo());
        sistemaConfigDAO.atualizar(existente);
        return existente;
    }

    public void excluir(Long idConfig) {
        sistemaConfigDAO.excluir(idConfig);
    }

    public List<SistemaConfigBean> listar() {
        return sistemaConfigDAO.listar();
    }
}
