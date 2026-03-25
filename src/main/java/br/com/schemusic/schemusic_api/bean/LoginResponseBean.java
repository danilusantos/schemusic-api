package br.com.schemusic.schemusic_api.bean;

public record LoginResponseBean(
        Long id,
        String nome,
        String perfil,
        String token,
        String idiomaPadrao
) {
}
