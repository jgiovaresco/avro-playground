package avro.playground;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(VertxExtension.class)
class AvroSerializerTest {

    private static final URL SERIALIZED_TRANSACTIONS_FILE = Objects.requireNonNull(AvroSerializerTest.class.getClassLoader().getResource("avro/transactions.avro"));
    private static final URL SERIALIZED_TRANSACTIONS_FILE_SCHEMA_LESS = Objects.requireNonNull(AvroSerializerTest.class.getClassLoader().getResource("avro/transactions_schemaless.avro"));

    AvroSerializer serializer;

    Schema schema;

    @BeforeEach
    void setUp(Vertx vertx) throws Exception {
        serializer = new AvroSerializer();

        var schemaFile = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("avro/transactions.avsc")).getFile()).toPath();
        schema = new AvroSchemaLoader(WebClient.create(vertx)).loadFromFile(schemaFile);
    }

    @Test
    public void should_serialize_record_to_file(@TempDir Path tempDir) throws IOException {
        File file = tempDir.resolve("transactions.avro").toFile();

        GenericRecord transaction1 = new GenericData.Record(schema);
        transaction1.put("id", "t1");
        transaction1.put("amount", 1500.0);

        serializer.serializeToFile(transaction1, file);

        byte[] dataOnly = Files.readAllBytes(Path.of(SERIALIZED_TRANSACTIONS_FILE_SCHEMA_LESS.getFile()));
        assertThat(file)
                .exists()
                .binaryContent().contains(dataOnly);
    }

    @Test
    public void should_serialize_json_input_to_avro_in_stream() throws IOException {
        var json = new JsonObject("""
                {
                  "id": "t1",
                  "amount": 1500.0
                }
                """);
        byte[] avroWithSchema = Files.readAllBytes(Path.of(SERIALIZED_TRANSACTIONS_FILE.getFile()));
        byte[] expected = Files.readAllBytes(Path.of(SERIALIZED_TRANSACTIONS_FILE_SCHEMA_LESS.getFile()));

        var avro = serializer.serializeJsonToAvroStream(json.toString(), schema);

        assertThat(avro).isEqualTo(expected);
        assertThat(avroWithSchema).contains(avro);
    }
}
