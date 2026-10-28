package com.smartroad.services.app.config;

import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;
import com.smartroad.services.common.util.NextentiConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:4000}")
    private String allowedOrigins;

    public SecurityConfiguration(AuthenticationProvider authenticationProvider, JwtAuthenticationFilter jwtAuthenticationFilter,
                                  OAuth2LoginSuccessHandler oauth2LoginSuccessHandler) {
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.oauth2LoginSuccessHandler = oauth2LoginSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handling -> handling.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/health").permitAll()
                        .requestMatchers("/api/v1/public/**").permitAll()
                        .requestMatchers("/api/v1/auth/signup", "/api/v1/auth/register").permitAll()
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/auth/refresh").permitAll()
                        .requestMatchers("/api/v1/auth/forgot-password").permitAll()
                        .requestMatchers("/api/v1/auth/reset-password").permitAll()
                        .requestMatchers("/api/v1/auth/verify-otp", "/api/v1/auth/verify-email").permitAll()
                        .requestMatchers("/api/v1/auth/resend-otp", "/api/v1/auth/resend-verification").permitAll()
                        .requestMatchers("/api/v1/auth/otp/**").permitAll()
                        .requestMatchers("/api/v1/admin/**", "/api/admin/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/swagger-resources/**", "/v2/api-docs", "/v3/api-docs", "/v3/api-docs/**", "/webjars/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2.successHandler(oauth2LoginSuccessHandler))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "Accept", "Origin",
                "Access-Control-Request-Method", "Access-Control-Request-Headers",
                "X-Requested-With",
                NextentiConstants.HEADER_PROJECT_ID, NextentiConstants.HEADER_SEARCH, NextentiConstants.HEADER_PAGE,
                NextentiConstants.HEADER_LIMIT, NextentiConstants.HEADER_MONTH
        ));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
