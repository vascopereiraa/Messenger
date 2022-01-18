package pt.isec.pd_g33.serverapi;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import pt.isec.pd_g33.serverapi.database.DatabaseManager;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

public class AuthorizationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        DatabaseManager dbmanager = new DatabaseManager();

        // Obtencao do header o token de autenticacao
        String token = request.getHeader("Authorization");

        //todo: Verifica se token é válido e devolve o username do user
        if (dbmanager.checkToken(token))
        {
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("USER"));

            UsernamePasswordAuthenticationToken uPAT = new UsernamePasswordAuthenticationToken(token, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(uPAT);
        }

        filterChain.doFilter(request, response);
        DatabaseManager.close();
    }



}
