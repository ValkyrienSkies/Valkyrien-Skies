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

package valkyrienwarfare.mixin.world;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;

import java.util.List;

@Mixin(World.class)
public abstract class MixinWorldCLIENT {

    @Shadow
    public int getLightFromNeighborsFor(EnumSkyBlock type, BlockPos pos) {
        return 0;
    }

    @Shadow
    public Chunk getChunkFromBlockCoords(BlockPos pos) {
        return null;
    }

    @Inject(method = "getCombinedLight(Lnet/minecraft/util/math/BlockPos;I)I", at = @At("HEAD"), cancellable = true)
    public void preGetCombinedLight(BlockPos pos, int lightValue, CallbackInfoReturnable callbackInfoReturnable) {
        try {
            int i = this.getLightFromNeighborsFor(EnumSkyBlock.SKY, pos);
            int j = this.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, pos);
            AxisAlignedBB lightBB = new AxisAlignedBB(pos.getX() - 2, pos.getY() - 2, pos.getZ() - 2, pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2);
            List<PhysicsWrapperEntity> physEnts = ValkyrienWarfareMod.physicsManager.getManagerForWorld(World.class.cast(this)).getNearbyPhysObjects(lightBB);

            for (PhysicsWrapperEntity physEnt : physEnts) {
                BlockPos posInLocal = RotationMatrices.applyTransform(physEnt.wrapping.coordTransform.wToLTransform, pos);
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

    @Shadow
    public abstract RayTraceResult rayTraceBlocks(Vec3d start, Vec3d end, boolean bool1, boolean bool2, boolean bool3);

    /**
     * This is easier to have as an overwrite because there's less laggy hackery to be done then :P
     *
     * @author DaPorkchop_
     */
    @Overwrite
    public BlockPos getPrecipitationHeight(BlockPos input) {
        BlockPos pos = this.getChunkFromBlockCoords(input).getPrecipitationHeight(input);
        if (ValkyrienWarfareMod.accurateRain) {
            Vector traceStart = new Vector(pos.getX() + .5D, Minecraft.getMinecraft().player.posY + 50D, pos.getZ() + .5D);
            Vector traceEnd = new Vector(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);
            RayTraceResult result = rayTraceBlocks(traceStart.toVec3d(), traceEnd.toVec3d(), true, true, false);

            if (result != null && result.typeOfHit != RayTraceResult.Type.MISS && result.getBlockPos() != null) {
                PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(World.class.cast(this), result.getBlockPos());
                if (wrapper != null) {
                    Vector blockPosVector = new Vector(result.getBlockPos().getX() + .5D, result.getBlockPos().getY() + .5D, result.getBlockPos().getZ() + .5D);
                    wrapper.wrapping.coordTransform.fromLocalToGlobal(blockPosVector);
                    BlockPos toReturn = new BlockPos(pos.getX(), blockPosVector.Y + .5D, pos.getZ());
                    return toReturn;
                }
            }
        }
        return pos;
    }
}
