package org.valkyrienskies.mixin.sponge_compatibility;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.valkyrienskies.mod.common.ships.QueryableShipData;
import org.valkyrienskies.mod.common.ships.ShipData;
import org.valkyrienskies.mod.common.ships.ShipDataMethods;

import java.util.Optional;

/**
 * This Mixin MUST load after MixinChunk from SpongeForge. So priority is set to 1001 to make this mixin load after SpongeForge's MixinChunk.
 */
@Mixin(value = Chunk.class, priority = 1001)
public abstract class MixinChunkSponge {

    @Shadow @Final
    public World world;

    @Inject(method = "bridge$setBlockState", at = @At("HEAD"), remap = false)
    private void onPreSpongeBridgeSetBlockState(BlockPos pos, IBlockState newState, IBlockState currentState, BlockChangeFlag flag, CallbackInfoReturnable<IBlockState> cir) {
        if (!world.isRemote) {
            QueryableShipData queryableShipData = QueryableShipData.get(world);
            Optional<ShipData> shipDataOptional = queryableShipData.getShipFromChunk(pos.getX() >> 4, pos.getZ() >> 4);
            shipDataOptional.ifPresent(shipData -> ShipDataMethods.onSetBlockState(shipData, pos, currentState, newState));
        }
    }

}