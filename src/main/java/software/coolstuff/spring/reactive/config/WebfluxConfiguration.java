package software.coolstuff.spring.reactive.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver;
import org.springframework.data.web.ReactiveSortHandlerMethodArgumentResolver;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.ViewResolverRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

@Configuration
@EnableWebFlux
public class WebfluxConfiguration implements WebFluxConfigurer {
  @Override
  public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
    configurer.addCustomResolver(new ReactivePageableHandlerMethodArgumentResolver());
    configurer.addCustomResolver(new ReactiveSortHandlerMethodArgumentResolver());
  }

  @Override
  public void configureViewResolvers(ViewResolverRegistry registry) {
    WebFluxConfigurer.super.configureViewResolvers(registry);
  }
}
