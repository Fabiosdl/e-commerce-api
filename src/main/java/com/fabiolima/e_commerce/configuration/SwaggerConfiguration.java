package com.fabiolima.e_commerce.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        String ecommerceAuth = "e-commerce-Auth";
        return new OpenAPI()
                .info(new Info()
                        .title("E-Commerce API")
                        .description("E-Commerce API")
                        .version("v1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(ecommerceAuth))
                .components(new Components().addSecuritySchemes(ecommerceAuth,
                        new SecurityScheme()
                                .name(ecommerceAuth)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
