package org.valkyrienskies.mod.common.util.jackson.minecraft;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import net.minecraft.util.math.AxisAlignedBB;

public class AxisAlignedBBSerialization {

    public static class Deserializer extends StdDeserializer<AxisAlignedBB> {

        public Deserializer() {
            this(null);
        }

        public Deserializer(Class<AxisAlignedBB> vc) {
            super(vc);
        }

        @Override
        public AxisAlignedBB deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
            JsonNode node = jp.getCodec().readTree(jp);

            double minX = node.get("minX").doubleValue();
            double minY = node.get("minY").doubleValue();
            double minZ = node.get("minZ").doubleValue();
            double maxX = node.get("maxX").doubleValue();
            double maxY = node.get("maxY").doubleValue();
            double maxZ = node.get("maxZ").doubleValue();

            return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
        }
    }

    public static class Serializer extends StdSerializer<AxisAlignedBB> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<AxisAlignedBB> vc) {
            super(vc);
        }


        @Override
        public void serialize(AxisAlignedBB value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
            gen.writeStartObject();
            gen.writeNumberField("minX", value.minX);
            gen.writeNumberField("minY", value.minY);
            gen.writeNumberField("minZ", value.minZ);
            gen.writeNumberField("maxX", value.maxX);
            gen.writeNumberField("maxY", value.maxY);
            gen.writeNumberField("maxZ", value.maxZ);
            gen.writeEndObject();
        }
    }



}