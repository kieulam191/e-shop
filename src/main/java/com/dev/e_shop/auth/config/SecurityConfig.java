package com.dev.e_shop.auth.config;

import com.dev.e_shop.auth.filter.JwtAuthTokenFilter;
import com.dev.e_shop.auth.handler.JwtAccessDenyEntryPoint;
import com.dev.e_shop.auth.handler.JwtAuthEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
public class SecurityConfig {
    private final JwtAuthTokenFilter jwtAuthTokenFilter;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final JwtAccessDenyEntryPoint jwtAccessDenyEntryPoint;

    public SecurityConfig(JwtAuthTokenFilter jwtAuthTokenFilter, JwtAuthEntryPoint jwtAuthEntryPoint, JwtAccessDenyEntryPoint jwtAccessDenyEntryPoint) {
        this.jwtAuthTokenFilter = jwtAuthTokenFilter;
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
        this.jwtAccessDenyEntryPoint = jwtAccessDenyEntryPoint;
    }

    @Bean
    SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http.csrf(crsf -> crsf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/public/**").permitAll()
                        .requestMatchers("/api/user/**").hasRole("USER")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(antMatcher("/h2-console/**")).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> {
                            ex.authenticationEntryPoint(jwtAuthEntryPoint);
                            ex.accessDeniedHandler(jwtAccessDenyEntryPoint);
                        }

                )
                .headers(h -> h.frameOptions(f -> f.disable()));

        return http.build();
    }
}
