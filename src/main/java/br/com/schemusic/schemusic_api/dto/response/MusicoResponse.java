package br.com.schemusic.schemusic_api.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MusicoResponse {
    private Long id;
    private String nome;
    private String estilo;
    private String contato;
    private String disponibilidade;
}
