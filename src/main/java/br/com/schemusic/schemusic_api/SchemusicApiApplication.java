package br.com.schemusic.schemusic_api;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.schemusic.schemusic_api.entity.AdminUser;
import br.com.schemusic.schemusic_api.repository.AdminUserRepository;

@SpringBootApplication
public class SchemusicApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SchemusicApiApplication.class, args);
	}
	
	@Bean CommandLineRunner init(AdminUserRepository repo, PasswordEncoder encoder) { 
		return args -> { 
			if (repo.findByUsername("admin").isEmpty()) { 
				AdminUser u = AdminUser.builder()
						.username("admin")
						.password(encoder.encode("admin123"))
						.roles("ROLE_ADMIN")
						.build();
				repo.save(u); 
				System.out.println("Admin inicial criado: username=admin, senha=admin123 (troque em produção)"); 
			} 
		}; 
	}

}
