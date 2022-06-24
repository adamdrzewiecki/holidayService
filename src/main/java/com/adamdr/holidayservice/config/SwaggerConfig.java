package com.adamdr.holidayservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                .securitySchemes(basicScheme())
                .securityContexts(securityContext())
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "Holiday Information Service API",
                "Rest API for Holiday Information Service.",
                "1.0",
                "",
                new Contact("Adam Drzewiecki", "", "adam.drzewiecki1988@gmail.com"),
                "",
                "",
                Collections.emptyList());
    }

    private List<SecurityScheme> basicScheme() {
        return List.of(HttpAuthenticationScheme.BASIC_AUTH_BUILDER.name("basicAuth").description("Basic authorization").build());
    }

    private SecurityReference basicAuthReference() {
        return new SecurityReference("basicAuth", new AuthorizationScope[0]);
    }

    private List<SecurityContext> securityContext() {
        return List.of(SecurityContext.builder().securityReferences(List.of(basicAuthReference())).build());
    }
}
