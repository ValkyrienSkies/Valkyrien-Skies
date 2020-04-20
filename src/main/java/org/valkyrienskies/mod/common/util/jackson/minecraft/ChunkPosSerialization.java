package org.valkyrienskies.mod.common.util.jackson.minecraft;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.minecraft.util.math.ChunkPos;

import java.io.IOException;

public class ChunkPosSerialization {

    public static class Deserializer extends StdDeserializer<ChunkPos> {

        public Deserializer() {
            this(null);
        }

        public Deserializer(Class<ChunkPos> vc) {
            super(vc);
        }

        @Override
        public ChunkPos deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
            JsonNode node = jp.getCodec().readTree(jp);

            long chunkPos = node.get("chunkPos").longValue();

            return new ChunkPos(getChunkX(chunkPos), getChunkZ(chunkPos));
        }
    }

    public static class Serializer extends StdSerializer<ChunkPos> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<ChunkPos> vc) {
            super(vc);
        }

        @Override
        public void serialize(ChunkPos value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
            gen.writeStartObject();
            gen.writeNumberField("chunkPos", ChunkPos.asLong(value.x, value.z));
            gen.writeEndObject();
        }
    }

    // Helper functions, not meant to be exposed outside of ChunkPosSerialization
    private static int getChunkX(long chunkPos) {
        return (int) (chunkPos & 4294967295L);
    }

    private static int getChunkZ(long chunkPos) {
        return (int) ((chunkPos >> 32) & 4294967295L);
    }

}