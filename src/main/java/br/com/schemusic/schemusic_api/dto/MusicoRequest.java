package br.com.schemusic.schemusic_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MusicoRequest {
    @NotBlank(message = "Nome é obrigatório")
    private String nome;
    private String estilo;
    private String contato;
    private String disponibilidade;
}
