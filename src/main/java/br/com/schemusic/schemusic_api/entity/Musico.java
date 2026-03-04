package br.com.schemusic.schemusic_api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "musicos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Musico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String estilo;

    private String contato;

    private String disponibilidade;
}
