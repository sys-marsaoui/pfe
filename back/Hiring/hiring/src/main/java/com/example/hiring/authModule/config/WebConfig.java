package com.example.hiring.authModule.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {



    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")  // Autoriser toutes les origines (vous pouvez également spécifier des domaines spécifiques)
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("Origin", "Content-Type", "Accept")
                .allowCredentials(false)
                .maxAge(3600);
    }


}
