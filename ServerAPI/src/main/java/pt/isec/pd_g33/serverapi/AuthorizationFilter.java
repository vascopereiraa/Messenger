package pt.isec.pd_g33.serverapi;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

public class AuthorizationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Obtencao do header o token de autenticacao
        String token = request.getHeader("Authorization");

        //todo: Verifica se token é válido e devolve o username do user
        String username = checkToken(token);
        if (token != null && username != null)
        {
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("USER"));

            UsernamePasswordAuthenticationToken uPAT = new UsernamePasswordAuthenticationToken(username, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(uPAT);
        }
        filterChain.doFilter(request, response);
    }

    private String checkToken(String token){
        try{
            Statement statement = db.createStatement();
            String sqlQuery = "SELECT token,username FROM User WHERE BINARY token = '" + token + "'";
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            if(resultSet.next()){
                statement.close();
                return resultSet.getString("username");
            }
            statement.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return null;
    }


    private static void close() {
        if(db != null) {
            try {
                db.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
