package org.valkyrienskies.mod.common.util.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.valkyrienskies.mod.common.util.jackson.minecraft.AxisAlignedBBSerialization;
import org.valkyrienskies.mod.common.util.jackson.minecraft.BlockPosSerialization;

public class MinecraftSerializationModule extends SimpleModule {

    public MinecraftSerializationModule() {
        super.addDeserializer(AxisAlignedBB.class, new AxisAlignedBBSerialization.Deserializer());
        super.addSerializer(AxisAlignedBB.class, new AxisAlignedBBSerialization.Serializer());

        super.addDeserializer(BlockPos.class, new BlockPosSerialization.Deserializer());
        super.addSerializer(BlockPos.class, new BlockPosSerialization.Serializer());
    }

}
