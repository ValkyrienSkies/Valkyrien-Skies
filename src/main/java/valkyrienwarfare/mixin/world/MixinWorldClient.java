/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2019 the Valkyrien Warfare team
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

package valkyrienwarfare.mixin.world;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.mod.common.ValkyrienWarfareMod;
import valkyrienwarfare.mod.common.entity.PhysicsWrapperEntity;
import valkyrienwarfare.mod.common.ship_handling.IHasShipManager;
import valkyrienwarfare.mod.common.ship_handling.WorldClientShipManager;

import java.util.List;

@Mixin(World.class)
public abstract class MixinWorldClient implements IHasShipManager {

    private final WorldClientShipManager manager = new WorldClientShipManager(World.class.cast(this));

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTickPost(CallbackInfo callbackInfo) {
        manager.tick();
    }

    @Shadow
    public abstract int getLightFromNeighborsFor(EnumSkyBlock type, BlockPos pos);

    @Shadow
    public abstract Chunk getChunk(BlockPos pos);

    @Inject(method = "getCombinedLight(Lnet/minecraft/util/math/BlockPos;I)I", at = @At("HEAD"), cancellable = true)
    private void preGetCombinedLight(BlockPos pos, int lightValue, CallbackInfoReturnable callbackInfoReturnable) {
        try {
            int i = this.getLightFromNeighborsFor(EnumSkyBlock.SKY, pos);
            int j = this.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, pos);
            AxisAlignedBB lightBB = new AxisAlignedBB(pos.getX() - 2, pos.getY() - 2, pos.getZ() - 2, pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2);
            List<PhysicsWrapperEntity> physEnts = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getManagerForWorld(World.class.cast(this)).getNearbyPhysObjects(lightBB);

            for (PhysicsWrapperEntity physEnt : physEnts) {
//                BlockPos posInLocal = RotationMatrices.applyTransform(physEnt.wrapping.coordTransform.wToLTransform, pos);
                BlockPos posInLocal = physEnt.getPhysicsObject().getShipTransformationManager().getCurrentTickTransform().transform(pos, TransformType.GLOBAL_TO_SUBSPACE);
                int localI = this.getLightFromNeighborsFor(EnumSkyBlock.SKY, posInLocal);
                int localJ = this.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, posInLocal);
                if (localI == 0 && localJ == 0) {
                    localI = this.getLightFromNeighborsFor(EnumSkyBlock.SKY, posInLocal.up());
                    localJ = this.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, posInLocal.up());
                }
                if (localI == 0 && localJ == 0) {
                    localI = this.getLightFromNeighborsFor(EnumSkyBlock.SKY, posInLocal.down());
                    localJ = this.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, posInLocal.down());
                }
                if (localI == 0 && localJ == 0) {
                    localI = this.getLightFromNeighborsFor(EnumSkyBlock.SKY, posInLocal.north());
                    localJ = this.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, posInLocal.north());
                }
                if (localI == 0 && localJ == 0) {
                    localI = this.getLightFromNeighborsFor(EnumSkyBlock.SKY, posInLocal.south());
                    localJ = this.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, posInLocal.south());
                }
                if (localI == 0 && localJ == 0) {
                    localI = this.getLightFromNeighborsFor(EnumSkyBlock.SKY, posInLocal.east());
                    localJ = this.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, posInLocal.east());
                }
                if (localI == 0 && localJ == 0) {
                    localI = this.getLightFromNeighborsFor(EnumSkyBlock.SKY, posInLocal.west());
                    localJ = this.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, posInLocal.west());
                }

                i = Math.min(localI, i);
                j = Math.max(localJ, j);
            }

            if (j < lightValue) {
                j = lightValue;
            }

            callbackInfoReturnable.setReturnValue(i << 20 | j << 4);
            callbackInfoReturnable.cancel();
            return;
        } catch (Exception e) {
            System.err.println("Something just went wrong here, getting default light value instead!!!!");
            e.printStackTrace();
        }
    }

    @Override
    public WorldClientShipManager getManager() {
        return manager;
    }
}
