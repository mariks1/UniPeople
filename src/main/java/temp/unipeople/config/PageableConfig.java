package temp.unipeople.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration
public class PageableConfig {
  @Bean
  PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer(
      @Value("${app.pagination.max-size:50}") int max) {
    return r -> {
      r.setOneIndexedParameters(false);
      r.setMaxPageSize(max);
      r.setFallbackPageable(PageRequest.of(0, 20));
    };
  }
}
