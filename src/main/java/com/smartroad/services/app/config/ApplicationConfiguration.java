package com.smartroad.services.app.config;

import com.smartroad.services.core.service.auth.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class ApplicationConfiguration implements WebMvcConfigurer {

    private final CustomUserDetailsService userDetailsService;
    private final UuidAuthenticationArgumentResolver uuidAuthenticationArgumentResolver;

    public ApplicationConfiguration(CustomUserDetailsService userDetailsService,
                                    UuidAuthenticationArgumentResolver uuidAuthenticationArgumentResolver) {
        this.userDetailsService = userDetailsService;
        this.uuidAuthenticationArgumentResolver = uuidAuthenticationArgumentResolver;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(uuidAuthenticationArgumentResolver);
    }
}
