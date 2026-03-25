package br.com.schemusic.schemusic_api.bean;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UsuarioBean {

    private Long idUsuario;
    private String nome;
    private String email;
    private String senha;
    private String idiomaPadrao;
    private Boolean ativo;
    private LocalDateTime dataCadastro;
    private List<String> roles = new ArrayList<>();

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getIdiomaPadrao() {
        return idiomaPadrao;
    }

    public void setIdiomaPadrao(String idiomaPadrao) {
        this.idiomaPadrao = idiomaPadrao;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
