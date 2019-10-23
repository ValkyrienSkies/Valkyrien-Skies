package org.valkyrienskies.mod.common.util.jackson.minecraft;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import net.minecraft.util.math.BlockPos;

public class BlockPosSerialization {

    public static class Deserializer extends StdDeserializer<BlockPos> {

        public Deserializer() {
            this(null);
        }

        public Deserializer(Class<BlockPos> vc) {
            super(vc);
        }

        @Override
        public BlockPos deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
            JsonNode node = jp.getCodec().readTree(jp);

            double x = node.get("x").doubleValue();
            double y = node.get("y").doubleValue();
            double z = node.get("z").doubleValue();

            return new BlockPos(x, y, z);
        }
    }

    public static class Serializer extends StdSerializer<BlockPos> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<BlockPos> vc) {
            super(vc);
        }


        @Override
        public void serialize(BlockPos value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
            gen.writeStartObject();;
            gen.writeNumberField("x", value.getX());
            gen.writeNumberField("y", value.getY());
            gen.writeNumberField("z", value.getZ());
            gen.writeEndObject();
        }
    }



}