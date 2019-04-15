/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.mixin.world.chunk;

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
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.mod.client.render.ITileEntitiesToRenderProvider;
import valkyrienwarfare.mod.physmanagement.chunk.PhysicsChunkManager;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mixin(value = Chunk.class, priority = 1001)
public abstract class MixinChunk implements ITileEntitiesToRenderProvider {

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
    public void post_addTileEntity(BlockPos pos, TileEntity tileEntityIn, CallbackInfo callbackInfo) {
        int yIndex = pos.getY() >> 4;
        removeTileEntityFromIndex(pos, yIndex);
        tileEntitesByExtendedData[yIndex].add(tileEntityIn);
        PhysicsWrapperEntity entity = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getObjectManagingPos(world, pos);
        if (entity != null) {
            entity.getPhysicsObject().onSetTileEntity(pos, tileEntityIn);
        }
    }

    @Inject(method = "removeTileEntity(Lnet/minecraft/util/math/BlockPos;)V", at = @At("TAIL"))
    public void post_removeTileEntity(BlockPos pos, CallbackInfo callbackInfo) {
        int yIndex = pos.getY() >> 4;
        removeTileEntityFromIndex(pos, yIndex);
        PhysicsWrapperEntity entity = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getObjectManagingPos(world, pos);
        if (entity != null) {
            entity.getPhysicsObject().onRemoveTileEntity(pos);
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

    @Inject(method = "Lnet/minecraft/world/chunk/Chunk;populate(Lnet/minecraft/world/chunk/IChunkProvider;Lnet/minecraft/world/gen/IChunkGenerator;)V", at = @At("HEAD"), cancellable = true)
    public void prePopulateChunk(IChunkProvider provider, IChunkGenerator generator, CallbackInfo callbackInfo) {
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
            Chunk realChunkFor = world.getChunkFromChunkCoords(i, j);
            if (!realChunkFor.isEmpty() && realChunkFor.loaded) {
                realChunkFor.addEntity(entityIn);
                callbackInfo.cancel(); // don't run the code on this chunk!!!
            }
        }
    }

    @Shadow
    public abstract IBlockState getBlockState(BlockPos pos);
}
