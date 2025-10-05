package com.insurai.backend.config;

import com.insurai.backend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors()
            .and()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/users/**").hasRole("ADMIN")

                // --- ALLOW DOCUMENT VIEWER ENDPOINT ---
                .requestMatchers(HttpMethod.GET, "/api/claims/view-document/**").permitAll()

                // Policies
                .requestMatchers(HttpMethod.GET, "/api/policies/**").hasAnyRole("ADMIN", "AGENT", "EMPLOYEE")
                .requestMatchers(HttpMethod.POST, "/api/policies/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/policies/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/policies/**").hasRole("ADMIN")

                // Claims
                .requestMatchers(HttpMethod.GET, "/api/claims/employee/**").hasAnyRole("EMPLOYEE", "ADMIN", "AGENT")
                .requestMatchers(HttpMethod.GET, "/api/claims/**").hasAnyRole("ADMIN", "AGENT")
                .requestMatchers(HttpMethod.POST, "/api/claims/**").hasAnyRole("ADMIN", "AGENT", "EMPLOYEE")
                .requestMatchers(HttpMethod.PUT, "/api/claims/**").hasAnyRole("ADMIN", "AGENT")
                .requestMatchers(HttpMethod.DELETE, "/api/claims/**").hasRole("ADMIN")

                // Claim notes
                .requestMatchers("/api/claim-notes/**").hasAnyRole("ADMIN", "AGENT", "EMPLOYEE")

                // Appointments
                .requestMatchers("/api/appointments/**").hasAnyRole("ADMIN", "AGENT", "EMPLOYEE")
                .requestMatchers("/api/employees/by-username/**").permitAll()

                // Employees
                .requestMatchers("/api/employees/**").hasAnyRole("ADMIN", "AGENT", "EMPLOYEE")

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .formLogin(form -> form.disable())
            .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
