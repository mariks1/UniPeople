package temp.unipeople.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  private static final String BEARER_AUTH = "bearerAuth";

  @Bean
  public OpenAPI baseOpenAPI() {
    var info =
        new Info()
            .title("UniPeople API")
            .version("v1")
            .description("HR/учёт сотрудников: CRUD, пагинация, отпуска и т.д.")
            .contact(new Contact().name("UniPeople Team").email("support@unipeople.local"));

    var server = new Server().url("/").description("default");

    var securityScheme =
        new SecurityScheme()
            .name(BEARER_AUTH)
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT");

    return new OpenAPI()
        .info(info)
        .servers(List.of(server))
        .components(
            new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes(BEARER_AUTH, securityScheme))
        .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
  }

  @Bean
  public GroupedOpenApi apiV1() {
    return GroupedOpenApi.builder().group("v1").pathsToMatch("/api/v1/**").build();
  }
}
