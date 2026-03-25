package br.com.schemusic.schemusic_api.bean;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestBean(
        @NotBlank(message = "Email obrigatorio")
        @Email(message = "Email invalido")
        String email,
        @NotBlank(message = "Senha obrigatoria")
        String senha
) {
}
