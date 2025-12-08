package com.khasanshin.fileservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver;
import org.springframework.data.web.ReactiveSortHandlerMethodArgumentResolver;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

@Configuration
public class WebFluxDataConfig implements WebFluxConfigurer {

    @Override
    public void configureArgumentResolvers(ArgumentResolverConfigurer cfg) {
        var sort = new ReactiveSortHandlerMethodArgumentResolver();
        var pageable = new ReactivePageableHandlerMethodArgumentResolver(sort);
        pageable.setMaxPageSize(50);
        cfg.addCustomResolver(sort);
        cfg.addCustomResolver(pageable);
    }
}
