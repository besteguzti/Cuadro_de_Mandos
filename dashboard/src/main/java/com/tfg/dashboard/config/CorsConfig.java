package com.tfg.dashboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    // =========================
    // Configuración CORS global
    // =========================
    //
    // Permite que el frontend React
    // ejecutado con Vite en localhost:5173
    // pueda consumir los endpoints REST
    // del backend Spring Boot.
    //
    // Esto aplica a:
    // - Aruba
    // - Citrix
    // - Microsoft 365
    // - GLPI
    // - Vista principal
    //

    @Bean
    public WebMvcConfigurer corsConfigurer() {

        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(
                    CorsRegistry registry
            ) {

                registry.addMapping("/**")

                        .allowedOrigins(
                                "http://localhost:5173"
                        )

                        .allowedMethods(
                                "GET",
                                "POST",
                                "PUT",
                                "DELETE",
                                "OPTIONS"
                        )

                        .allowedHeaders("*");
            }
        };
    }
}