package co.edu.escuelaing.twitter.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3.0 configuration. Accessible at /swagger-ui/index.html
 */
@Configuration
public class OpenApiConfig {

    @Value("${auth0.domain}")
    private String auth0Domain;

    @Value("${auth0.audience}")
    private String audience;

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
            .info(apiInfo())
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local development"),
                new Server().url("https://api.your-domain.com").description("Production (AWS API Gateway)")
            ))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description(
                            "Obtain a JWT from Auth0:\n" +
                            "POST https://" + auth0Domain + "/oauth/token\n" +
                            "Audience: " + audience
                        )
                )
            );
    }

    private Info apiInfo() {
        return new Info()
            .title("Twitter-like API")
            .version("1.0.0")
            .description(
                "RESTful API for a Twitter-like microblogging platform. " +
                "Authenticated users can create posts (max 140 chars). " +
                "All posts are visible in a public global stream."
            )
            .contact(new Contact()
                .name("Escuela Colombiana de Ingeniería Julio Garavito")
                .email("anderssondavidsanchez@gmail.com")
            )
            .license(new License().name("MIT").url("https://opensource.org/licenses/MIT"));
    }
}
