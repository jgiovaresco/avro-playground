package avro.playground;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AvroDeserializer {

    public List<GenericRecord> deserializeFromFile(File source, Schema schema) {
        List<GenericRecord> records = new ArrayList<>();

        DatumReader<GenericRecord> datumWriter = new GenericDatumReader<>(schema);
        try (DataFileReader<GenericRecord> dataFileReader = new DataFileReader<>(source, datumWriter)) {

            while (dataFileReader.hasNext()) {
                records.add(dataFileReader.next());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while serializing a record", e);
        }

        return records;
    }


    public String deserializeAvroToJson(byte[] avroBytes, Schema schema) throws IOException {
        GenericDatumReader<Object> reader = new GenericDatumReader<>(schema);
        DatumWriter<Object> writer = new GenericDatumWriter<>(schema);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        JsonEncoder encoder = EncoderFactory.get().jsonEncoder(schema, output);
        encoder.setIncludeNamespace(true);
        Decoder decoder = DecoderFactory.get().binaryDecoder(avroBytes, null);
        Object datum = reader.read(null, decoder);
        writer.write(datum, encoder);
        encoder.flush();
        output.flush();

        return output.toString(StandardCharsets.UTF_8);
    }
}
