package br.com.schemusic.schemusic_api.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.schemusic.schemusic_api.entity.AdminUser;
import br.com.schemusic.schemusic_api.repository.AdminUserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminUserRepository repo;

    public CustomUserDetailsService(AdminUserRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminUser user = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        String[] roles = user.getRoles().replace("ROLE_", "").split(",");
        return User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(roles)
                .build();
    }
}
