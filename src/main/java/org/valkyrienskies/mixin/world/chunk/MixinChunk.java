package org.valkyrienskies.mixin.world.chunk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.fixes.IPhysicsChunk;
import org.valkyrienskies.mod.client.render.ITileEntitiesToRenderProvider;
import org.valkyrienskies.mod.common.physics.management.PhysicsObject;
import org.valkyrienskies.mod.common.physmanagement.chunk.PhysicsChunkManager;

@Mixin(value = Chunk.class, priority = 1001)
public abstract class MixinChunk implements ITileEntitiesToRenderProvider, IPhysicsChunk {

    private Optional<PhysicsObject> parentOptional = Optional.empty();

    @Shadow
    public abstract World getWorld();

    @Override
    public void setParentPhysicsObject(Optional<PhysicsObject> physicsObjectOptional) {
        this.parentOptional = physicsObjectOptional;
    }

    @Override
    public Optional<PhysicsObject> getPhysicsObjectOptional() {
        return parentOptional;
    }

    @Shadow
    @Final
    public int x;

    @Shadow
    @Final
    public int z;

    @Shadow
    @Final
    public World world;

    // We keep track of these so we can quickly update the tile entities that need rendering.
    private List<TileEntity>[] tileEntitesByExtendedData = new List[16];

    public List<TileEntity> getTileEntitiesToRender(int chunkExtendedDataIndex) {
        return tileEntitesByExtendedData[chunkExtendedDataIndex];
    }

    @Inject(method = "addTileEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/tileentity/TileEntity;)V", at = @At("TAIL"))
    public void post_addTileEntity(BlockPos pos, TileEntity tileEntityIn,
        CallbackInfo callbackInfo) {
        int yIndex = pos.getY() >> 4;
        removeTileEntityFromIndex(pos, yIndex);
        tileEntitesByExtendedData[yIndex].add(tileEntityIn);
        if (getPhysicsObjectOptional().isPresent()) {
            getPhysicsObjectOptional().get()
                .onSetTileEntity(pos, tileEntityIn);
        }
    }

    @Inject(method = "removeTileEntity(Lnet/minecraft/util/math/BlockPos;)V", at = @At("TAIL"))
    public void post_removeTileEntity(BlockPos pos, CallbackInfo callbackInfo) {
        int yIndex = pos.getY() >> 4;
        removeTileEntityFromIndex(pos, yIndex);
        if (getPhysicsObjectOptional().isPresent()) {
            getPhysicsObjectOptional().get()
                .onRemoveTileEntity(pos);
        }
    }

    private void removeTileEntityFromIndex(BlockPos pos, int yIndex) {
        if (tileEntitesByExtendedData[yIndex] == null) {
            tileEntitesByExtendedData[yIndex] = new ArrayList<TileEntity>();
        }
        Iterator<TileEntity> tileEntitiesIterator = tileEntitesByExtendedData[yIndex].iterator();
        while (tileEntitiesIterator.hasNext()) {
            TileEntity tile = tileEntitiesIterator.next();
            if (tile.getPos().equals(pos) || tile.isInvalid()) {
                tileEntitiesIterator.remove();
            }
        }
    }

    @Inject(method = "populate(Lnet/minecraft/world/chunk/IChunkProvider;Lnet/minecraft/world/gen/IChunkGenerator;)V", at = @At("HEAD"), cancellable = true)
    public void prePopulateChunk(IChunkProvider provider, IChunkGenerator generator,
        CallbackInfo callbackInfo) {
        if (PhysicsChunkManager.isLikelyShipChunk(this.x, this.z)) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "addEntity(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    public void preAddEntity(Entity entityIn, CallbackInfo callbackInfo) {
        World world = this.world;

        int i = MathHelper.floor(entityIn.posX / 16.0D);
        int j = MathHelper.floor(entityIn.posZ / 16.0D);

        if (i == this.x && j == this.z) {
            // do nothing, and let vanilla code take over after our injected code is done
            // (now)
        } else {
            Chunk realChunkFor = world.getChunk(i, j);
            if (!realChunkFor.isEmpty() && realChunkFor.loaded) {
                realChunkFor.addEntity(entityIn);
                callbackInfo.cancel(); // don't run the code on this chunk!!!
            }
        }
    }

    @Shadow
    public abstract IBlockState getBlockState(BlockPos pos);
}
