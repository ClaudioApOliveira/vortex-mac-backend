package com.vortex.shared.openapi;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;

@OpenAPIDefinition(
    info =
        @Info(
            title = "Vortex - API da Oficina",
            version = "1.0.0",
            description = "API REST do sistema de gestão da oficina mecânica MATRA",
            contact = @Contact(name = "Vortex")))
@SecurityScheme(
    securitySchemeName = OpenApiConfig.BEARER_AUTH,
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "JWT obtido via POST /api/auth/login ou POST /api/auth/primeiro-acesso")
public class OpenApiConfig {

  public static final String BEARER_AUTH = "bearerAuth";
}
