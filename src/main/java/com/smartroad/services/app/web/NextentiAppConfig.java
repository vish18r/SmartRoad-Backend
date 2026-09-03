package com.smartroad.services.app.web;

import com.smartroad.services.api.interceptor.RequestHeaderInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@PropertySources({
        @PropertySource("classpath:application.properties"),
        @PropertySource("classpath:messages.properties"),
        @PropertySource(value = "classpath:dbConfig.properties", ignoreResourceNotFound = true)
})
public class NextentiAppConfig implements WebMvcConfigurer {

    private final RequestHeaderInterceptor requestHeaderInterceptor;

    public NextentiAppConfig(RequestHeaderInterceptor requestHeaderInterceptor) {
        this.requestHeaderInterceptor = requestHeaderInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestHeaderInterceptor);
    }
}
