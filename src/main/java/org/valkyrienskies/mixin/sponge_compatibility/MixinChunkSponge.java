package org.valkyrienskies.mixin.sponge_compatibility;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.valkyrienskies.mod.common.ships.QueryableShipData;
import org.valkyrienskies.mod.common.ships.ShipData;
import org.valkyrienskies.mod.common.ships.ShipDataMethods;

import java.util.Optional;

/**
 * This Mixin MUST load after MixinChunk from SpongeForge, otherwise the @Intrinsic displacement won't work.
 * So priority is set to 1001 to make this mixin load after SpongeForge's MixinChunk.
 */
@Mixin(value = Chunk.class, priority = 1001)
@Implements(@Interface(iface = ChunkBridge.class, prefix = "valkyrienskies$"))
public abstract class MixinChunkSponge implements ChunkBridge {

    @Shadow @Final
    public World world;

    @Intrinsic(displace = true)
    public IBlockState valkyrienskies$bridge$setBlockState(BlockPos pos, IBlockState newState, IBlockState currentState, BlockChangeFlag flag) {
        if (!world.isRemote) {
            QueryableShipData queryableShipData = QueryableShipData.get(world);
            Optional<ShipData> shipDataOptional = queryableShipData.getShipFromChunk(pos.getX() >> 4, pos.getZ() >> 4);
            shipDataOptional.ifPresent(shipData -> ShipDataMethods.onSetBlockState(shipData, pos, currentState, newState));
        }
        return bridge$setBlockState(pos, newState, currentState, flag);
    }
}