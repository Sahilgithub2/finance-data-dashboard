package com.finance.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String JWT_SCHEME = "bearer-jwt";

    @Bean
    public OpenAPI financeDashboardOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Finance Dashboard API")
                                .version("1.0")
                                .description(
                                        """
                                        REST API for authentication, user administration, and transactions.

                                        **RBAC:** `ADMIN` — full users API and all transactions; `ANALYST` — all transactions (no users API); `VIEWER` — own data only (`GET /api/transactions` returns **403**).

                                        **Authorize:** call **POST /api/auth/login**, copy the `token` value, open **Authorize** here and paste (Swagger sends `Bearer` for you).

                                        **GraphQL** is separate from this document: **POST /graphql** with the same JWT, or use **/graphiql** when enabled.""")
                                .contact(new Contact().name("Finance Dashboard").email("dev@finance.local"))
                                .license(new License().name("Proprietary").url("https://example.com")))
                .servers(List.of(new Server().url("/").description("This application")))
                .externalDocs(
                        new ExternalDocumentation()
                                .description("GraphQL schema & dashboard queries")
                                .url("/graphql/schema"))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        JWT_SCHEME,
                                        new SecurityScheme()
                                                .name(JWT_SCHEME)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .description(
                                                        "Obtain via **Authentication → POST /api/auth/login**. "
                                                                + "Paste the JSON `token` value.")))
                .addSecurityItem(new SecurityRequirement().addList(JWT_SCHEME));
    }
}
