package com.vibelink.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import org.springframework.context.annotation.Configuration;


@OpenAPIDefinition(
        info = @Info(title = "VibeLink REST API", version = "v1", description = "Spotify Blend-like API"),
        security = {@SecurityRequirement(name = "AppToken")}
)
@SecuritySchemes({
        @SecurityScheme(name = "AppToken", type = io.swagger.v3.oas.annotations.enums.SecuritySchemeType.APIKEY,
                paramName = "X-App-Token", in = io.swagger.v3.oas.annotations.enums.SecuritySchemeIn.HEADER)
})
@Configuration
public class OpenApiConfig { }