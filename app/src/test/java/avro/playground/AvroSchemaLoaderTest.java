package avro.playground;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.apache.avro.Schema;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ExtendWith(VertxExtension.class)
class AvroSchemaLoaderTest {
    private static final String SCHEMA_REGISTRY_SERVICE = "schema-registry";
    private static final int SCHEMA_REGISTRY_SERVICE_PORT = 8081;

    WebClient client;
    AvroSchemaLoader schemaLoader;
    String schemaRegistryUrl;

    @Container
    static DockerComposeContainer<?> environment =
            new DockerComposeContainer<>(new File("src/test/resources/docker/docker-compose.yml"))
                    .withExposedService(
                            SCHEMA_REGISTRY_SERVICE,
                            SCHEMA_REGISTRY_SERVICE_PORT,
                            Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30))
                    );

    @BeforeEach
    void setUp(Vertx vertx) {
        client = WebClient.create(vertx);

        schemaLoader = new AvroSchemaLoader(client);
        schemaRegistryUrl = String.format(
                "http://%s:%s",
                environment.getServiceHost(SCHEMA_REGISTRY_SERVICE, SCHEMA_REGISTRY_SERVICE_PORT),
                environment.getServicePort(SCHEMA_REGISTRY_SERVICE, SCHEMA_REGISTRY_SERVICE_PORT)
        );
    }

    @Test
    public void should_load_from_a_file() throws IOException {
        var file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("avro/transactions.avsc")).getFile()).toPath();
        var schema = schemaLoader.loadFromFile(file);

        assertSchema(schema);
    }

    @Test
    public void should_load_from_a_remote_registry(VertxTestContext context) {
        schemaLoader.loadFromRegistry(schemaRegistryUrl + "/schemas/ids/1", "fred", "letmein".getBytes())
                .onSuccess(schema -> {
                    assertSchema(schema.orElseThrow());
                    context.completeNow();
                })
                .onFailure(context::failNow);
    }

    @Test
    public void should_return_an_empty_optional_when_the_schema_is_not_found(VertxTestContext context) {
        schemaLoader.loadFromRegistry(schemaRegistryUrl + "/schemas/ids/12", "fred", "letmein".getBytes())
                .onSuccess(schema -> {
                    assertThat(schema).isEmpty();
                    context.completeNow();
                })
                .onFailure(context::failNow);
    }

    private void assertSchema(Schema schema) {
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(schema.getName()).isEqualTo("Payment");
            soft.assertThat(schema.getNamespace()).isEqualTo("io.confluent.examples.clients.basicavro");
        });
    }
}
