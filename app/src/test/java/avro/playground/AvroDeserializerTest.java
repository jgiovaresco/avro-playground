package avro.playground;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(VertxExtension.class)
class AvroDeserializerTest {

    private static final URL SERIALIZED_TRANSACTIONS_FILE = Objects.requireNonNull(AvroSerializerTest.class.getClassLoader().getResource("avro/transactions.avro"));
    private static final URL SERIALIZED_TRANSACTIONS_FILE_SCHEMA_LESS = Objects.requireNonNull(AvroSerializerTest.class.getClassLoader().getResource("avro/transactions_schemaless.avro"));

    AvroDeserializer deserializer;

    Schema schema;

    @BeforeEach
    void setUp(Vertx vertx) throws Exception {
        deserializer = new AvroDeserializer();

        var schemaFile = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("avro/transactions.avsc")).getFile()).toPath();
        schema = new AvroSchemaLoader(WebClient.create(vertx)).loadFromFile(schemaFile);
    }

    @Test
    public void should_deserialize_record_from_file() {
        var source = new File(SERIALIZED_TRANSACTIONS_FILE.getFile());
        List<GenericRecord> transactions = deserializer.deserializeFromFile(source, schema);

        assertThat(transactions).hasSize(1).element(0).satisfies(item -> SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(item.hasField("id")).describedAs("should contain id field").isTrue();
            soft.assertThat(item.hasField("amount")).describedAs("should contain amount field").isTrue();
            soft.assertThat(item.get("id").toString()).isEqualTo("t1");
            soft.assertThat(item.get("amount")).isEqualTo(1500.0);
        }));
    }


    @Test
    public void should_deserialize_avro_binary_to_json() throws Exception {
        byte[] avro = Files.readAllBytes(Path.of(SERIALIZED_TRANSACTIONS_FILE_SCHEMA_LESS.getFile()));

        String json = deserializer.deserializeAvroToJson(avro, schema);

        SoftAssertions.assertSoftly(soft -> {
            var item = new JsonObject(json);
            soft.assertThat(item.getString("id")).isEqualTo("t1");
            soft.assertThat(item.getDouble("amount")).isEqualTo(1500.0);
        });
    }
}
