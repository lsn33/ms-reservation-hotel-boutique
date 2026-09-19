package com.hotelboutique.reservas.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Catalogo de habitaciones: publico, no requiere login
                        .requestMatchers(HttpMethod.GET, "/habitaciones/**").permitAll()

                        // Crear/gestionar habitaciones: solo staff del hotel
                        .requestMatchers(HttpMethod.POST, "/habitaciones").hasRole("ADMIN")

                        // Ver TODAS las reservas (de todos los huespedes): solo ADMIN
                        .requestMatchers(HttpMethod.GET, "/reservas").hasRole("ADMIN")

                        // Check-in/check-out/cancelar: operaciones de recepcion, solo ADMIN
                        .requestMatchers(HttpMethod.PUT, "/reservas/*/checkin").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/reservas/*/checkout").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/reservas/*/cancelar").hasRole("ADMIN")

                        // Crear reserva y ver "mis reservas": cualquier usuario autenticado (CLIENTE o ADMIN)
                        .requestMatchers(HttpMethod.POST, "/reservas").authenticated()
                        .requestMatchers(HttpMethod.GET, "/reservas/mias").authenticated()

                        .requestMatchers("/h2-console/**").permitAll() // solo para dev con H2
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}