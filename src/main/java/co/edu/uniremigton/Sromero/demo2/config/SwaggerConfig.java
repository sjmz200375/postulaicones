package co.edu.uniremigton.Sromero.demo2.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Sistema de Postulaciones API")
                .version("1.0.0")
                .description("API REST para gestión de postulaciones institucionales. " +
                    "Los endpoints marcados con el candado requieren el header X-API-Key. " +
                    "Los endpoints públicos son: POST /api/postulaciones y POST /api/usuarios/login.")
                .contact(new Contact()
                    .name("Administración del Sistema")
                    .email("admin@institucion.edu.co")))
            .addSecurityItem(new SecurityRequirement().addList("X-API-Key"))
            .components(new Components()
                .addSecuritySchemes("X-API-Key", new SecurityScheme()
                    .name("X-API-Key")
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.HEADER)
                    .description("API Key requerida para endpoints protegidos. " +
                        "Valor: postulaciones-2026-secret-key")));
    }
}
