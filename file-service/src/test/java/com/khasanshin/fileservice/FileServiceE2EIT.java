package com.khasanshin.fileservice;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.main.allow-bean-definition-overriding=true",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
@Import(TestSecurityConfig.class)
@Testcontainers
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FileServiceE2EIT {

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:16");

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE)
            .withDatabaseName("unipeople")
            .withUsername("postgres")
            .withPassword("postgres");

    @Container
    static GenericContainer<?> minio = new GenericContainer<>("minio/minio:latest")
            .withEnv("MINIO_ROOT_USER", "minio")
            .withEnv("MINIO_ROOT_PASSWORD", "minio12345")
            .withCommand("server /data --console-address :9001")
            .withExposedPorts(9000, 9001);

    private static final String BUCKET = "files";

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        Startables.deepStart(Stream.of(postgres, minio)).join();
        registry.add("spring.r2dbc.url", () ->
                "r2dbc:postgresql://" + postgres.getHost() + ":" + postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT) + "/" + postgres.getDatabaseName());
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);

        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);

        registry.add("s3.endpoint", () -> "http://" + minio.getHost() + ":" + minio.getMappedPort(9000));
        registry.add("s3.region", () -> "us-east-1");
        registry.add("s3.access-key", () -> "minio");
        registry.add("s3.secret-key", () -> "minio12345");
        registry.add("s3.bucket", () -> BUCKET);
    }

    @Autowired
    WebTestClient webTestClient;

    @BeforeAll
    void createBucket() {
        S3Client client = S3Client.builder()
                .endpointOverride(URI.create("http://" + minio.getHost() + ":" + minio.getMappedPort(9000)))
                .forcePathStyle(true)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("minio", "minio12345")))
                .region(Region.US_EAST_1)
                .build();
        try {
            client.createBucket(CreateBucketRequest.builder().bucket(BUCKET).build());
        } catch (Exception ignored) {
        }
    }

    @Test
    void uploadAndListFile_throughHttp_withPostgresAndMinio() {
        UUID ownerId = UUID.randomUUID();

        var fileContent = "hello-testcontainers".getBytes(StandardCharsets.UTF_8);
        var fileResource = new ByteArrayResource(fileContent) {
            @Override
            public String getFilename() {
                return "hello.txt";
            }
        };

        MultipartBodyBuilder body = new MultipartBodyBuilder();
        body.part("file", fileResource)
                .filename("hello.txt")
                .contentType(MediaType.TEXT_PLAIN);
        body.part("meta", """
                        {"owner_id":"%s","owner_type":"TEST","category":"DOC"}
                        """.formatted(ownerId))
                .contentType(MediaType.APPLICATION_JSON);

        webTestClient.post()
                .uri("/api/v1/files")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(org.springframework.web.reactive.function.BodyInserters.fromMultipartData(body.build()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.owner_id").isEqualTo(ownerId.toString())
                .jsonPath("$.original_name").isEqualTo("hello.txt");

        var response = webTestClient.get()
                .uri("/api/v1/files?owner_id={owner}", ownerId)
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class);

        var body2 = response.getResponseBody().blockFirst();
        assertThat(body2).contains("hello.txt");
        assertThat(response.getResponseHeaders().getFirst("X-Total-Count")).isNotBlank();
    }
}
