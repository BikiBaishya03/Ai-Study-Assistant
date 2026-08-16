package com.v1.Ai_Study_Assistant_Backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults()) // 1. Tell Spring Security to enable CORS
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/study/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }

    // 2. Define the actual CORS rules
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow your React frontend
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));

        // Allow all standard HTTP methods, including OPTIONS (needed for preflight)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allow all headers
        configuration.setAllowedHeaders(List.of("*"));

        // Important if you ever add authentication cookies/tokens later
        configuration.setAllowCredentials(true);

        // Apply these rules to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
