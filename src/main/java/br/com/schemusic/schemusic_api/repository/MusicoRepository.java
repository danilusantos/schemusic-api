package br.com.schemusic.schemusic_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.schemusic.schemusic_api.entity.Musico;

@Repository
public interface MusicoRepository extends JpaRepository<Musico, Long> {
}
