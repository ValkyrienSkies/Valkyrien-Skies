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

package valkyrienwarfare.mixin.entity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.math.VWMath;
import valkyrienwarfare.math.Vector;
import valkyrienwarfare.mod.coordinates.ISubspacedEntity;
import valkyrienwarfare.mod.coordinates.ISubspacedEntityRecord;
import valkyrienwarfare.mod.coordinates.VectorImmutable;
import valkyrienwarfare.physics.collision.polygons.EntityPolygon;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

import java.util.List;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity implements ISubspacedEntity {

    private final EntityLivingBase thisAsEntity = EntityLivingBase.class.cast(this);

    /**
     * This constructor is needed to make java compile this class, but doesn't actually affect anything
     */
    public MixinEntityLivingBase(World world) {
        super(world);
    }

    @Override
    public void restoreSubspacedEntityStateToRecord(ISubspacedEntityRecord record) {
        VectorImmutable coordinates = record.getPosition();
        VectorImmutable coordinatesLastTick = record.getPositionLastTick();
        VectorImmutable lookVector = record.getLookDirection();
        VectorImmutable velocityVector = record.getVelocity();

        thisAsEntity.lastTickPosX = coordinatesLastTick.getX();
        thisAsEntity.lastTickPosY = coordinatesLastTick.getY();
        thisAsEntity.lastTickPosZ = coordinatesLastTick.getZ();

        double pitch = VWMath.getPitchFromVectorImmutable(lookVector);
        double yaw = VWMath.getYawFromVectorImmutable(lookVector, pitch);

        this.rotationPitch = (float) pitch;
        this.rotationYaw = (float) yaw;

        this.motionX = velocityVector.getX();
        this.motionY = velocityVector.getY();
        this.motionZ = velocityVector.getZ();

        thisAsEntity.setPosition(coordinates.getX(), coordinates.getY(), coordinates.getZ());
    }

    @Inject(method = "dismountEntity", at = @At("HEAD"), cancellable = true)
    public void dismountEntity(Entity entityIn, CallbackInfo info) {
        if (entityIn instanceof PhysicsWrapperEntity) {
            this.ridingEntity = null;
            this.posY += 1.45D;
            info.cancel(); // effectively a premature method return
        }
    }

    /**
     * This is easier to have as an overwrite because there's less laggy hackery to be done then :P
     *
     * @author DaPorkchop_
     */
    @Overwrite
    public boolean isOnLadder() {
        boolean vanilla = this.isOnLadderOriginalButSlightlyOptimized();
        if (vanilla) {
            return true;
        }
        if (EntityPlayer.class.isInstance(this) && EntityPlayer.class.cast(this).isSpectator()) {
            return false;
        }
        List<PhysicsWrapperEntity> nearbyPhys = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getManagerForWorld(this.world).getNearbyPhysObjects(this.getEntityBoundingBox());
        for (PhysicsWrapperEntity physWrapper : nearbyPhys) {
            Vector playerPos = new Vector(EntityLivingBase.class.cast(this));
            physWrapper.getPhysicsObject().getShipTransformationManager().fromGlobalToLocal(playerPos);
            int i = MathHelper.floor(playerPos.X);
            int j = MathHelper.floor(playerPos.Y);
            int k = MathHelper.floor(playerPos.Z);

            BlockPos blockpos = new BlockPos(i, j, k);
            IBlockState iblockstate = this.world.getBlockState(blockpos);
            Block block = iblockstate.getBlock();

            /*boolean isSpectator = (EntityPlayer.class.isInstance(this) && EntityPlayer.class.cast(this).isSpectator());
            if (isSpectator)
                return false;*/
            //not needed, we already do this check

            EntityPolygon playerPoly = new EntityPolygon(this.getEntityBoundingBox(), physWrapper.getPhysicsObject().getShipTransformationManager().getCurrentTickTransform(), TransformType.GLOBAL_TO_SUBSPACE, this);
            AxisAlignedBB bb = playerPoly.getEnclosedAABB();
            for (int x = MathHelper.floor(bb.minX); x < bb.maxX; x++) {
                for (int y = MathHelper.floor(bb.minY); y < bb.maxY; y++) {
                    for (int z = MathHelper.floor(bb.minZ); z < bb.maxZ; z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        IBlockState checkState = this.world.getBlockState(pos);
                        if (checkState.getBlock().isLadder(checkState, this.world, pos, EntityLivingBase.class.cast(this))) {
                            return true;
                            // AxisAlignedBB ladderBB = checkState.getBlock().getBoundingBox(checkState, base.worldObj, pos).offset(pos).expandXyz(.1D);
                            // Polygon checkBlock = new Polygon(ladderBB);
                            // EntityPolygonCollider collider = new EntityPolygonCollider(playerPoly, checkBlock, physWrapper.wrapping.coordTransform.normals, new Vector(base.motionX,base.motionY,base.motionZ));
                            //// System.out.println(!collider.seperated);
                            // if(!collider.seperated){
                            // return true;
                            // }
                        }
                    }
                }
            }

            // return net.minecraftforge.common.ForgeHooks.isLivingOnLadder(iblockstate, base.worldObj, new BlockPos(i, j, k), base);
        }
        return false;
    }

    public boolean isOnLadderOriginalButSlightlyOptimized() {
        int i = MathHelper.floor(this.posX);
        int j = MathHelper.floor(this.getEntityBoundingBox().minY);
        int k = MathHelper.floor(this.posZ);
        BlockPos blockpos = new BlockPos(i, j, k);
        IBlockState iblockstate = this.world.getBlockState(blockpos);
        return net.minecraftforge.common.ForgeHooks.isLivingOnLadder(iblockstate, world, new BlockPos(i, j, k), EntityLivingBase.class.cast(this));
    }
}
