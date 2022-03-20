package software.coolstuff.spring.reactive;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.r2dbc.AutoConfigureDataR2dbc;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import software.coolstuff.spring.reactive.config.DatabaseConfiguration;
import software.coolstuff.spring.reactive.config.WebfluxConfiguration;
import software.coolstuff.spring.reactive.controller.PetController;
import software.coolstuff.spring.reactive.entity.PetEntity;
import software.coolstuff.spring.reactive.mapper.PetMapper;
import software.coolstuff.spring.reactive.model.PetModel;
import software.coolstuff.spring.reactive.service.PetService;
import software.coolstuff.spring.reactive.utils.ControllerSupport;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = PetController.class, properties = {
    "debug=true",
    "spring.flyway.url=jdbc:h2:file:./target/testdb;DB_CLOSE_DELAY=-1",
    "spring.flyway.user=sa",
    "spring.r2dbc.url=r2dbc:h2:file:///./target/testdb",
    "spring.r2dbc.username=sa"
})
@ContextConfiguration(classes = PetControllerTest.Configuration.class)
@AutoConfigureDataR2dbc
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, statements = {
    "INSERT INTO pets (id, type, name) VALUES (1, 'CAT', 'Luna')",
    "INSERT INTO pets (id, type, name) VALUES (2, 'CAT', 'Murli')",
    "INSERT INTO pets (id, type, name) VALUES (3, 'DOG', 'Amadeus')"
})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = "TRUNCATE TABLE pets")
class PetControllerTest {
  @TestConfiguration
  @EnableAutoConfiguration
  @Import({PetController.class, WebfluxConfiguration.class, DatabaseConfiguration.class})
  @ComponentScan(basePackageClasses = PetService.class, useDefaultFilters = false, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PetService.class))
  @ComponentScan(basePackageClasses = PetMapper.class, useDefaultFilters = false, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PetMapper.class))
  public static class Configuration {
    @Bean
    public DataSource dataSource(@Value("${spring.flyway.url}") final String url, @Value("${spring.flyway.user}") final String username) {
      final var dataSource = new DriverManagerDataSource();
      dataSource.setUrl(url);
      dataSource.setUsername(username);
      return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(final DataSource dataSource) {
      return new JdbcTemplate(dataSource);
    }
  }

  @Autowired
  private WebTestClient webClient;

  @Autowired
  private JdbcOperations jdbcOperations;

  @AfterAll
  static void afterAll() throws IOException {
    Files.deleteIfExists(Paths.get("./target/testdb.mv.db"));
  }

  @Test
  @DisplayName("GET /" + PetController.ENDPOINT + " and expect 3 Entries (verified by JSON-Path)")
  void ok_list_withoutFilter_verifiedByJSONPath() {
    webClient.get().uri("/" + PetController.ENDPOINT)
             .exchange()
             .expectStatus().isOk()
             .expectHeader().contentType(MediaType.APPLICATION_JSON)
             .expectBody()
             .jsonPath("$").isArray()
             .jsonPath("$").isNotEmpty()
             .jsonPath("$.length()").isEqualTo(3)
             .jsonPath("$[0].id").isEqualTo(3)
             .jsonPath("$[0].version").isEqualTo(1)
             .jsonPath("$[0].type").isEqualTo(PetModel.Type.DOG.toString())
             .jsonPath("$[0].name").isEqualTo("Amadeus")
             .jsonPath("$[1].id").isEqualTo(1)
             .jsonPath("$[1].version").isEqualTo(1)
             .jsonPath("$[1].type").isEqualTo(PetModel.Type.CAT.toString())
             .jsonPath("$[1].name").isEqualTo("Luna")
             .jsonPath("$[2].id").isEqualTo(2)
             .jsonPath("$[2].version").isEqualTo(1)
             .jsonPath("$[2].type").isEqualTo(PetModel.Type.CAT.toString())
             .jsonPath("$[2].name").isEqualTo("Murli");
  }

  @Test
  @DisplayName("GET /" + PetController.ENDPOINT + " and expect 3 Entries (verified by JSON-Mapping)")
  void ok_list_withoutFiler_verifiedByJSONMapping() {
    webClient.get().uri("/" + PetController.ENDPOINT)
             .exchange()
             .expectStatus().isOk()
             .expectHeader().contentType(MediaType.APPLICATION_JSON)
             .expectBodyList(PetModel.class)
             .hasSize(3)
             .isEqualTo(List.of(
                 PetModel.builder()
                         .id(3L)
                         .version(1)
                         .type(PetModel.Type.DOG)
                         .name("Amadeus")
                         .build(),
                 PetModel.builder()
                         .id(1L)
                         .version(1)
                         .type(PetModel.Type.CAT)
                         .name("Luna")
                         .build(),
                 PetModel.builder()
                         .id(2L)
                         .version(1)
                         .type(PetModel.Type.CAT)
                         .name("Murli")
                         .build()));
  }

  @Test
  @DisplayName("GET /" + PetController.ENDPOINT + "/name=a and expect 2 Entries (verified by JSON-Mapping)")
  void ok_list_withFilter_verifiedByJSONMapping() {
    webClient.get().uri(UriComponentsBuilder.fromPath("/" + PetController.ENDPOINT)
                                            .queryParam("name", "a")
                                            .toUriString())
             .exchange()
             .expectStatus().isOk()
             .expectHeader().contentType(MediaType.APPLICATION_JSON)
             .expectBodyList(PetModel.class)
             .hasSize(2)
             .isEqualTo(List.of(
                 PetModel.builder()
                         .id(3L)
                         .version(1)
                         .type(PetModel.Type.DOG)
                         .name("Amadeus")
                         .build(),
                 PetModel.builder()
                         .id(1L)
                         .version(1)
                         .type(PetModel.Type.CAT)
                         .name("Luna")
                         .build()));
  }

  @Test
  @DisplayName("GET /" + PetController.ENDPOINT + "/name=zzz and expect no Entries")
  void nok_list_withFilter() {
    webClient.get().uri(UriComponentsBuilder.fromPath("/" + PetController.ENDPOINT)
                                            .queryParam("name", "zzz")
                                            .toUriString())
             .exchange()
             .expectStatus().isOk()
             .expectHeader().contentType(MediaType.APPLICATION_JSON)
             .expectBody()
             .jsonPath("$").isArray()
             .jsonPath("$").isEmpty();
  }

  @Test
  @DisplayName("GET /" + PetController.ENDPOINT + "/1 and expect getting Cat Luna")
  void ok_get() {
    webClient.get().uri("/" + PetController.ENDPOINT + "/{id}", 1)
             .exchange()
             .expectStatus().isOk()
             .expectHeader().contentType(MediaType.APPLICATION_JSON)
             .expectBody(PetModel.class)
             .isEqualTo(PetModel.builder()
                                .id(1L)
                                .version(1)
                                .type(PetModel.Type.CAT)
                                .name("Luna")
                                .build());
  }

  @Test
  @DisplayName("GET /" + PetController.ENDPOINT + "/99999 and expect no Pet")
  void nok_get() {
    webClient.get().uri("/" + PetController.ENDPOINT + "/{id}", 99999)
             .exchange()
             .expectStatus().isNotFound();
  }

  @Test
  @DisplayName("POST /" + PetController.ENDPOINT + " and expect the Pet has been created within the Database and returned")
  void ok_create() {
    webClient.post().uri("/" + PetController.ENDPOINT)
             .bodyValue(PetModel.builder()
                                .type(PetModel.Type.CAT)
                                .name("Tom")
                                .build())
             .exchange()
             .expectStatus().isCreated()
             .expectHeader().contentType(MediaType.APPLICATION_JSON)
             .expectBody(PetModel.class)
             .isEqualTo(PetModel.builder()
                                .id(4L)
                                .version(1)
                                .type(PetModel.Type.CAT)
                                .name("Tom")
                                .build());

    assertThat(getPetsFromDatabase())
        .isNotEmpty()
        .hasSize(4)
        .contains(PetEntity.builder()
                           .id(4L)
                           .version(1)
                           .type(PetModel.Type.CAT)
                           .name("Tom")
                           .build());
  }

  private List<PetEntity> getPetsFromDatabase() {
    return jdbcOperations.query("SELECT id, version, type, name FROM " + PetEntity.TABLE_NAME, this::mapToPetEntity);
  }

  private PetEntity mapToPetEntity(ResultSet resultSet, int rowNum) throws SQLException {
    return PetEntity.builder()
                    .id(resultSet.getLong("id"))
                    .version(resultSet.getInt("version"))
                    .type(PetModel.Type.valueOf(resultSet.getString("type")))
                    .name(resultSet.getString("name"))
                    .build();
  }

  @Test
  @DisplayName("PUT /" + PetController.ENDPOINT + "/1 and expect the Name of the Cat has been changed from Luna to Moon with an incremented Version")
  void ok_update() {
    webClient.put().uri("/" + PetController.ENDPOINT + "/{id}", 1)
             .bodyValue(PetModel.builder()
                                .id(1L)
                                .version(1)
                                .type(PetModel.Type.CAT)
                                .name("Moon")
                                .build())
             .exchange()
             .expectStatus().isOk()
             .expectHeader().contentType(MediaType.APPLICATION_JSON)
             .expectBody(PetModel.class)
             .isEqualTo(PetModel.builder()
                                .id(1L)
                                .version(2)
                                .type(PetModel.Type.CAT)
                                .name("Moon")
                                .build());

    assertThat(getPetFromDatabase(1L))
        .isPresent()
        .get()
        .isEqualTo(PetEntity.builder()
                            .id(1L)
                            .version(2)
                            .type(PetModel.Type.CAT)
                            .name("Moon")
                            .build());
  }

  private Optional<PetEntity> getPetFromDatabase(long id) {
    try {
      return Optional.ofNullable(jdbcOperations.queryForObject("SELECT id, version, type, name FROM " + PetEntity.TABLE_NAME + " WHERE id = " + id, this::mapToPetEntity));
    } catch (EmptyResultDataAccessException ignored) {
      return Optional.empty();
    }
  }

  @Test
  @DisplayName("PUT /" + PetController.ENDPOINT + "/99999 and expect HTTP 404 - Not found")
  void nok_update() {
    webClient.put().uri("/" + PetController.ENDPOINT + "/{id}", 99999)
             .bodyValue(PetModel.builder()
                                .id(99999L)
                                .version(1)
                                .type(PetModel.Type.CAT)
                                .name("Not Exists")
                                .build())
             .exchange()
             .expectStatus().isNotFound();
  }

  @Test
  @DisplayName("DELETE /" + PetController.ENDPOINT + "/1 and expect the Cat Luna has been removed")
  void ok_delete() {
    webClient.delete().uri("/" + PetController.ENDPOINT + "/{id}", 1)
             .header(ControllerSupport.HEADER_VERSION, "1")
             .exchange()
             .expectStatus().isNoContent()
             .expectBody().isEmpty();

    assertThat(getPetFromDatabase(1L)).isNotPresent();
  }
}
