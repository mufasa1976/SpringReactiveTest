package software.coolstuff.spring.reactive.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import software.coolstuff.spring.reactive.entity.PetEntity;

@Configuration
@EnableR2dbcRepositories(basePackageClasses = PetEntity.class)
public class DatabaseConfiguration {}
