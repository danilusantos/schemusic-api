package br.com.schemusic.schemusic_api.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaResponse {
    private Long id;
    private String nome;
    private String cnpj;
    private String contato;
    private String endereco;
}
