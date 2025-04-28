package com.example.configserver.config;

import com.example.configserver.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Permit OPTIONS requests for CORS preflight
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // Password reset endpoints (public)
                .requestMatchers("/api/users/forgot-password").permitAll()
                .requestMatchers("/api/users/validate-token").permitAll()
                .requestMatchers("/api/users/reset-password").permitAll()
                // Read-only endpoints (accessible by both ADMIN and READ_ONLY)
                .requestMatchers(HttpMethod.GET, "/api/groups/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/items/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/environments/**").permitAll()
                // Admin-only endpoints (write operations)
                .requestMatchers(HttpMethod.POST, "/api/groups/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/groups/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/groups/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/items/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/items/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/items/**").hasAuthority("ADMIN")
                // User management endpoints (admin only)
                .requestMatchers(HttpMethod.POST, "/api/users").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/users/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/users/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAuthority("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 