package com.veterinaria.veterinariaAnimalVet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import com.veterinaria.veterinariaAnimalVet.security.JwtAuthenticationFilter;
import com.veterinaria.veterinariaAnimalVet.service.UsuarioService;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    FilterRegistrationBean<JwtAuthenticationFilter> disableServletFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/app/**", "/error", "/api", "/api/", "/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers("/admin/roles/**").denyAll()
                        .anyRequest().authenticated())
                .logout(logout -> logout.disable())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) -> {
                            Object originalUri = request.getAttribute(jakarta.servlet.RequestDispatcher.ERROR_REQUEST_URI);
                            if (request.getRequestURI().startsWith("/api/") || (originalUri instanceof String uri && uri.startsWith("/api/"))) response.sendError(401);
                            else response.sendRedirect("/app/login");
                        })
                        .accessDeniedHandler((request, response, exception) -> response.sendError(403)))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    AuthenticationManager authenticationManager(UsuarioService usuarios, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(usuarios);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
