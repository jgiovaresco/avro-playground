package avro.playground;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import org.apache.avro.Schema;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public class AvroSchemaLoader {
    private final WebClient client;

    public AvroSchemaLoader(WebClient client) {
        this.client = client;
    }

    public Future<Optional<Schema>> loadFromRegistry(String url, String username, byte[] password) {
        return client
                .getAbs(url)
                .basicAuthentication(Buffer.buffer(username), Buffer.buffer(password))
                .as(BodyCodec.jsonObject())
                .send()
                .map(response -> {
                    if (response.statusCode() == 200) {
                        var json = response.body();
                        return Optional.ofNullable(new Schema.Parser().parse(json.getString("schema")));
                    }

                    return Optional.empty();
                });
    }

    public Schema loadFromFile(Path file) throws IOException {
        return new Schema.Parser().parse(file.toFile());
    }
}
