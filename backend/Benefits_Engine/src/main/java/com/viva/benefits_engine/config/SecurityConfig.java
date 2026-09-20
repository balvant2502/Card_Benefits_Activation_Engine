package com.viva.benefits_engine.config;

import com.viva.benefits_engine.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    /**
     * Prevent Spring Boot from auto-registering JwtAuthenticationFilter as a
     * standalone servlet filter. It must only run inside the Spring Security
     * filter chain (added via addFilterBefore), otherwise it executes twice and
     * the second pass has no security context, causing permitAll() routes like
     * /api/auth/** to return 401.
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(
            JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json");
                            response.setStatus(401);
                            response.getWriter().write("{\"error\": \"Unauthorized\"}");
                        }))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/transactions/**").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers("/api/eligibility/**").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers("/api/claims/**").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers("/api/claim-notifications/**").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers("/api/benefits/**").hasRole("ADMIN")
                        .requestMatchers("/api/metrics/**").hasRole("ADMIN")
                        .requestMatchers("/api/cards/*/benefits/*").hasRole("ADMIN")
                        .requestMatchers("/api/cards/*/benefits/*/rules").hasRole("ADMIN")
                        .requestMatchers("/api/cards/**").hasAnyRole("CUSTOMER", "ADMIN")
                        .anyRequest().authenticated());

        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
