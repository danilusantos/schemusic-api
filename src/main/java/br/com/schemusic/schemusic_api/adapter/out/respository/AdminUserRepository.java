package br.com.schemusic.schemusic_api.adapter.out.respository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.schemusic.schemusic_api.domain.entity.AdminUser;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
	Optional<AdminUser> findByUsername(String username);
}
