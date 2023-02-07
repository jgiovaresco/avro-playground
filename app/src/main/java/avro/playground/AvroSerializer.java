package avro.playground;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class AvroSerializer {

    public void serializeToFile(GenericRecord record, File target) {
        var schema = record.getSchema();
        var datumWriter = new GenericDatumWriter<GenericRecord>(schema);

        try (DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter)) {
            dataFileWriter.create(schema, target);
            dataFileWriter.append(record);
        } catch (IOException e) {
            throw new RuntimeException("Error while serializing a record", e);
        }
    }

    public byte[] serializeJsonToAvroStream(String json, Schema schema) throws IOException {
        DatumReader<Object> reader = new GenericDatumReader<>(schema);
        GenericDatumWriter<Object> writer = new GenericDatumWriter<>(schema);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        Decoder decoder = DecoderFactory.get().jsonDecoder(schema, json);
        Encoder encoder = EncoderFactory.get().binaryEncoder(output, null);

        Object datum = reader.read(null, decoder);

        writer.write(datum, encoder);
        encoder.flush();

        return output.toByteArray();
    }
}
