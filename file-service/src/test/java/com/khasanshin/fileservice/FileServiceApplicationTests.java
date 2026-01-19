package com.khasanshin.fileservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import java.util.stream.Stream;

@SpringBootTest(
		properties = {
				"spring.cloud.config.enabled=false",
				"eureka.client.enabled=false",
				"spring.main.allow-bean-definition-overriding=true",
				"server.port=0"
		}
)
@Testcontainers
class FileServiceApplicationTests {

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
		registry.add("s3.bucket", () -> "files");
	}

	@Test
	void contextLoads() {
	}
}
