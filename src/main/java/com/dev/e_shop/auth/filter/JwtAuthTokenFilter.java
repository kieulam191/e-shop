package com.dev.e_shop.auth.filter;

import com.dev.e_shop.auth.JwtService;
import com.dev.e_shop.user.UserDetail;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthTokenFilter extends OncePerRequestFilter {
    private final String JWT_TYPE = "Bearer";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthTokenFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith(JWT_TYPE)){
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        try{
            String token = authHeader.split(" ")[1];
            String email = jwtService.extractUsername(token);
            if (email != null && authentication == null) {
                UserDetail userDetails = (UserDetail) userDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (ExpiredJwtException ex) {
            request.setAttribute("jwt_exception", ex);
        } catch (SignatureException ex) {
            request.setAttribute("jwt_exception", ex);
        } catch (MalformedJwtException ex) {
            request.setAttribute("jwt_exception", ex);
        } catch (JwtException ex) {
            request.setAttribute("jwt_exception", ex);
        } catch (UsernameNotFoundException ex) {
            request.setAttribute("jwt_exception", ex);
        }


        filterChain.doFilter(request, response);
    }

}
