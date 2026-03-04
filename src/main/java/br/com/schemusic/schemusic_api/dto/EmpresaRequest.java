package br.com.schemusic.schemusic_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaRequest {
    @NotBlank(message = "Nome é obrigatório")
    private String nome;
    private String cnpj;
    private String contato;
    private String endereco;
}
