package org.valkyrienskies.mod.common.network;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import net.minecraft.util.math.AxisAlignedBB;

import java.io.IOException;

public class AxisAlignedBBSerializer extends StdDeserializer<AxisAlignedBB> {

    public AxisAlignedBBSerializer() {
        this(null);
    }

    public AxisAlignedBBSerializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public AxisAlignedBB deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);

        double minX = (Double) node.get("minX").numberValue();
        double minY = (Double) node.get("minY").numberValue();
        double minZ = (Double) node.get("minZ").numberValue();
        double maxX = (Double) node.get("maxX").numberValue();
        double maxY = (Double) node.get("maxY").numberValue();
        double maxZ = (Double) node.get("maxZ").numberValue();

        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
