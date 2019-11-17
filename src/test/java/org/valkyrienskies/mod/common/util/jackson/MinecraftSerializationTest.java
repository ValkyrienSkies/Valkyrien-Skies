package org.valkyrienskies.mod.common.util.jackson;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import java.io.IOException;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MinecraftSerializationTest {

    static ObjectMapper mapper;

    @BeforeAll
    public static void setupMapper() {
        mapper = new CBORMapper();
        mapper.registerModule(new MinecraftSerializationModule());

        mapper.setVisibility(mapper.getVisibilityChecker()
            .withFieldVisibility(Visibility.ANY)
            .withGetterVisibility(Visibility.NONE)
            .withIsGetterVisibility(Visibility.NONE)
            .withSetterVisibility(Visibility.NONE));
    }

    @Test
    public void axisAlignedBBSerialization() throws IOException {
        AxisAlignedBB expected = new AxisAlignedBB(10, 10, 10, 50, 50, 50);
        byte[] serialized = mapper.writeValueAsBytes(expected);
        AxisAlignedBB actual = mapper.readValue(serialized, AxisAlignedBB.class);

        assertThat(expected, equalTo(actual));
    }

    @Test
    public void blockPosSerialization() throws IOException {
        BlockPos expected = new BlockPos(20.5, 13.333, 11.3);
        byte[] serialized = mapper.writeValueAsBytes(expected);
        BlockPos actual = mapper.readValue(serialized, BlockPos.class);

        assertThat(expected, equalTo(actual));
    }
}
