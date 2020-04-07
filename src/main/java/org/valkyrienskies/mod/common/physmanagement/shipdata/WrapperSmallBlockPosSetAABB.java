package org.valkyrienskies.mod.common.physmanagement.shipdata;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.mod.common.physics.collision.meshing.IVoxelFieldAABBMaker;
import org.valkyrienskies.mod.common.physics.collision.meshing.NaiveVoxelFieldAABBMaker;
import org.valkyrienskies.mod.common.physmanagement.shipdata.WrapperSmallBlockPosSetAABB.WrapperSmallBlockPosSetAABBSerializer;
import org.valkyrienskies.mod.common.util.VSIterationUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Iterator;

/**
 * A wrapper around SmallBlockPosSet and IVoxelFieldAABBMaker that implements IBlockPosSetAABB behavior.
 */
@JsonDeserialize(using = WrapperSmallBlockPosSetAABB.WrapperSmallBlockPosSetAABBDeserializer.class)
@JsonSerialize(using = WrapperSmallBlockPosSetAABBSerializer.class)
public class WrapperSmallBlockPosSetAABB implements IBlockPosSetAABB {

    private final SmallBlockPosSet blockPosSet;
    private final IVoxelFieldAABBMaker aabbMaker;

    public WrapperSmallBlockPosSetAABB(SmallBlockPosSet blockPosSet, IVoxelFieldAABBMaker aabbMaker) {
        this.blockPosSet = blockPosSet;
        this.aabbMaker = aabbMaker;
    }

    @Nullable
    @Override
    public AxisAlignedBB makeAABB() {
        return aabbMaker.makeVoxelFieldAABB();
    }

    @Override
    public boolean add(int x, int y, int z) throws IllegalArgumentException {
        boolean setResult = blockPosSet.add(x, y, z);
        boolean makerResult = aabbMaker.addVoxel(x, y, z);
        if (setResult != makerResult) {
            throw new IllegalStateException("IBlockPosSet and IVoxelFieldAABBMaker state does not match! Pos causing is " + new BlockPos(x, y, z));
        }
        return setResult;
    }

    @Override
    public boolean remove(int x, int y, int z) {
        boolean setResult = blockPosSet.remove(x, y, z);
        boolean makerResult = aabbMaker.removeVoxel(x, y, z);
        if (setResult != makerResult) {
            throw new IllegalStateException("IBlockPosSet and IVoxelFieldAABBMaker state does not match! Pos causing is " + new BlockPos(x, y, z));
        }
        return setResult;
    }

    @Override
    public boolean contains(int x, int y, int z) {
        return blockPosSet.contains(x, y, z);
    }

    @Override
    public boolean canStore(int x, int y, int z) {
        return blockPosSet.canStore(x, y, z);
    }

    @Override
    public int size() {
        int setResult = blockPosSet.size();
        int makerResult = aabbMaker.size();
        if (setResult != makerResult) {
            throw new IllegalStateException("IBlockPosSet and IVoxelFieldAABBMaker state does not match!");
        }
        return setResult;
    }

    @NotNull
    @Override
    public Iterator<BlockPos> iterator() {
        return blockPosSet.iterator();
    }

    @Override
    public void clear() {
        blockPosSet.clear();
        aabbMaker.clear();
    }

    @Override
    public void forEach(@Nonnull VSIterationUtils.IntTernaryConsumer action) {
        blockPosSet.forEach(action);
    }

    public static class WrapperSmallBlockPosSetAABBSerializer extends StdSerializer<WrapperSmallBlockPosSetAABB> {

        public WrapperSmallBlockPosSetAABBSerializer() {
            super((Class<WrapperSmallBlockPosSetAABB>) null);
        }

        @Override
        public void serialize(WrapperSmallBlockPosSetAABB value, JsonGenerator gen,
                              SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            gen.writeObjectField("blockPosSet", value.blockPosSet);
            gen.writeEndObject();
        }
    }

    public static class WrapperSmallBlockPosSetAABBDeserializer extends StdDeserializer<WrapperSmallBlockPosSetAABB> {

        private final ObjectMapper objectMapper = new ObjectMapper();

        public WrapperSmallBlockPosSetAABBDeserializer() {
            super((Class<?>) null);
        }

        @Override
        public WrapperSmallBlockPosSetAABB deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            // The blockPosSet gets loaded
            SmallBlockPosSet blockPosSet = objectMapper.treeToValue(node.get("blockPosSet"), SmallBlockPosSet.class);
            // The AABB maker is a derivative of the blockPosSet.
            IVoxelFieldAABBMaker aabbMaker = new NaiveVoxelFieldAABBMaker(blockPosSet.getCenterX(), blockPosSet.getCenterZ());
            blockPosSet.forEach((VSIterationUtils.IntTernaryConsumer) aabbMaker::addVoxel);
            return new WrapperSmallBlockPosSetAABB(blockPosSet, aabbMaker);
        }
    }
}
