package com.example.springboot.employee;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;



@Configuration
public class SwaggerConfig {

    @Bean
    GroupedOpenApi publicApi(){
        return GroupedOpenApi.builder()
                .group("public-apis")
                .pathsToMatch("/**")
                .build();
    }

    @Bean
    OpenAPI customOpenAPI(){
        return new OpenAPI()
                .info(new Info().title("Employee").version("1.0"))
                .addSecurityItem(new SecurityRequirement().addList("Yash"))
                .components(new Components()
                        .addSecuritySchemes("Yash", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));

    }
}
