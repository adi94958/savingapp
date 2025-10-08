package com.adisaputera.savingapp.config;

import org.springframework.lang.NonNull;  
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;

import java.util.List;

@Configuration
public class WebConfig {
    @Value("${app.cors.allowed-origins:*}")
    private List<String> allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns(allowedOrigins.toArray(String[]::new))
                        .allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Authorization","Content-Disposition")
                        .allowCredentials(true);
            }
        };
    }
}
