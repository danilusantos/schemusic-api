package br.com.schemusic.schemusic_api;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.schemusic.schemusic_api.adapter.out.respository.AdminUserRepository;
import br.com.schemusic.schemusic_api.adapter.out.respository.EmpresaRepository;
import br.com.schemusic.schemusic_api.adapter.out.respository.MusicoRepository;
import br.com.schemusic.schemusic_api.domain.entity.AdminUser;
import br.com.schemusic.schemusic_api.domain.entity.Empresa;
import br.com.schemusic.schemusic_api.domain.entity.Musico;

@SpringBootApplication
public class SchemusicApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SchemusicApiApplication.class, args);
	}

	@Bean
	CommandLineRunner init(AdminUserRepository repo, PasswordEncoder encoder) {
		return args -> {
			if (repo.findByUsername("admin").isEmpty()) {
				AdminUser u = AdminUser.builder().username("admin").password(encoder.encode("admin123"))
						.roles("ROLE_ADMIN").build();
				repo.save(u);
			}
		};
	}

	@Bean
	CommandLineRunner seed(MusicoRepository musRepo, EmpresaRepository empRepo) {
		return args -> {
			if (musRepo.count() == 0) {
				musRepo.save(Musico.builder().nome("João Silva").estilo("MPB").contato("11 99999-0000")
						.disponibilidade("Fins de semana").build());
			}
			if (empRepo.count() == 0) {
				empRepo.save(Empresa.builder().nome("Casa de Shows").cnpj("12345678000199")
						.contato("contato@casadeshows.com").endereco("Rua A, 123").build());
			}
		};
	}

}
