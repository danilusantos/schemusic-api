package br.com.schemusic.schemusic_api.business;

import br.com.schemusic.schemusic_api.bean.RoleBean;
import br.com.schemusic.schemusic_api.bean.UsuarioBean;
import br.com.schemusic.schemusic_api.dao.RoleDAO;
import br.com.schemusic.schemusic_api.dao.UsuarioRoleDAO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminRoleBusiness {

    private final RoleDAO roleDAO;
    private final UsuarioRoleDAO usuarioRoleDAO;

    public AdminRoleBusiness(RoleDAO roleDAO, UsuarioRoleDAO usuarioRoleDAO) {
        this.roleDAO = roleDAO;
        this.usuarioRoleDAO = usuarioRoleDAO;
    }

    public RoleBean criar(String nome, String descricao, Boolean ativo) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome da role obrigatorio");
        }
        if (roleDAO.buscarPorNome(nome.trim().toUpperCase()) != null) {
            throw new IllegalArgumentException("Role ja existe");
        }

        RoleBean role = new RoleBean();
        role.setNome(nome.trim().toUpperCase());
        role.setDescricao(descricao);
        role.setAtivo(ativo == null || ativo);

        Long id = roleDAO.inserir(role);
        return roleDAO.buscarPorId(id);
    }

    public RoleBean editar(Long idRole, String nome, String descricao, Boolean ativo) {
        RoleBean existente = roleDAO.buscarPorId(idRole);
        if (existente == null) {
            throw new IllegalArgumentException("Role nao encontrada");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome da role obrigatorio");
        }

        RoleBean porNome = roleDAO.buscarPorNome(nome.trim().toUpperCase());
        if (porNome != null && !porNome.getIdRole().equals(idRole)) {
            throw new IllegalArgumentException("Ja existe role com este nome");
        }

        existente.setNome(nome.trim().toUpperCase());
        existente.setDescricao(descricao);
        existente.setAtivo(ativo != null ? ativo : existente.getAtivo());
        roleDAO.atualizar(existente);
        return roleDAO.buscarPorId(idRole);
    }

    public void excluir(Long idRole) {
        RoleBean role = roleDAO.buscarPorId(idRole);
        if (role == null) {
            throw new IllegalArgumentException("Role nao encontrada");
        }
        if (usuarioRoleDAO.existeVinculoComRole(idRole)) {
            throw new IllegalArgumentException("Nao e possivel excluir role com usuarios vinculados");
        }
        roleDAO.excluir(idRole);
    }

    public List<RoleBean> listar() {
        return roleDAO.listar();
    }

    public List<UsuarioBean> listarUsuariosDaRole(Long idRole) {
        if (roleDAO.buscarPorId(idRole) == null) {
            throw new IllegalArgumentException("Role nao encontrada");
        }
        return roleDAO.listarUsuariosDaRole(idRole);
    }
}
