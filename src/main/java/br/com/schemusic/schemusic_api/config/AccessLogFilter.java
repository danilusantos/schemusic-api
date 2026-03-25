package br.com.schemusic.schemusic_api.config;

import br.com.schemusic.schemusic_api.bean.AcessoSiteLogBean;
import br.com.schemusic.schemusic_api.dao.AcessoSiteLogDAO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AccessLogFilter extends OncePerRequestFilter {

    private final AcessoSiteLogDAO acessoSiteLogDAO;

    public AccessLogFilter(AcessoSiteLogDAO acessoSiteLogDAO) {
        this.acessoSiteLogDAO = acessoSiteLogDAO;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        filterChain.doFilter(request, response);

        String caminho = request.getRequestURI();
        if (caminho.startsWith("/error") || caminho.startsWith("/favicon")) {
            return;
        }

        AcessoSiteLogBean log = new AcessoSiteLogBean();
        log.setMetodo(request.getMethod());
        log.setCaminho(caminho);
        log.setIp(request.getRemoteAddr());
        log.setStatusHttp(response.getStatus());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            log.setUserId(authentication.getName());
        }

        try {
            acessoSiteLogDAO.inserir(log);
        } catch (RuntimeException ignored) {
            // O log de acesso nao deve interromper o fluxo principal da API.
        }
    }
}
