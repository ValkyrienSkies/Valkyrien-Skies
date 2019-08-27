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

package org.valkyrienskies.mod.common.physmanagement.interaction;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.valkyrienskies.fixes.IPhysicsChunk;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.coordinates.CoordinateSpaceType;
import org.valkyrienskies.mod.common.entity.EntityMountable;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;
import org.valkyrienskies.mod.common.math.RotationMatrices;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

import java.util.Optional;

public class VWWorldEventListener implements IWorldEventListener {

    private final World worldObj;

    public VWWorldEventListener(World world) {
        worldObj = world;
    }

    @Override
    public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
        Chunk chunk = worldIn.getChunk(pos);
        IPhysicsChunk physicsChunk = (IPhysicsChunk) chunk;
        if (physicsChunk.getPhysicsObjectOptional()
                .isPresent()) {
            physicsChunk.getPhysicsObjectOptional()
                    .get()
                    .onSetBlockState(oldState, newState, pos);
        }
    }

    @Override
    public void notifyLightSet(BlockPos pos) {
        // TODO Auto-generated method stub

    }

    @Override
    public void markBlockRangeForRenderUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        // this.markBlocksForUpdate(minX - 1, minY - 1, minZ - 1, maxX + 1, maxY + 1, maxZ + 1, false);
    }

    @Override
    public void playSoundToAllNearExcept(EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x,
                                         double y, double z, float volume, float pitch) {

    }

    @Override
    public void playRecord(SoundEvent soundIn, BlockPos pos) {
        // TODO Auto-generated method stub

    }

    @Override
    public void spawnParticle(int particleID, boolean ignoreRange, double x, double y, double z, double xSpeed,
                              double ySpeed, double zSpeed, int... parameters) {
        // TODO Auto-generated method stub

    }

    // TODO: Fix conflicts with EventsCommon.onEntityJoinWorldEvent()
    @Override
    public void onEntityAdded(Entity entity) {
        if (entity instanceof PhysicsWrapperEntity) {
            ValkyrienSkiesMod.VW_PHYSICS_MANAGER.onShipLoad((PhysicsWrapperEntity) entity);
        } else {
            // This is really only here because Sponge doesn't call the entity join event for some reason :/
            // So I basically just copied the event code here as well.
            World world = worldObj;
            BlockPos posAt = new BlockPos(entity);
            Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysicsObject(world, posAt);

            if (!worldObj.isRemote && physicsObject.isPresent() && !(entity instanceof EntityFallingBlock)) {
                if (entity instanceof EntityArmorStand
                        || entity instanceof EntityPig || entity instanceof EntityBoat) {
                    EntityMountable entityMountable = new EntityMountable(world, entity.getPositionVector(), CoordinateSpaceType.SUBSPACE_COORDINATES, posAt);
                    world.spawnEntity(entityMountable);
                    entity.startRiding(entityMountable);
                }
                world.getChunk(entity.getPosition().getX() >> 4, entity.getPosition().getZ() >> 4).removeEntity(entity);
                RotationMatrices.applyTransform(physicsObject.get()
                                .getShipTransformationManager()
                                .getCurrentTickTransform(), entity,
                        TransformType.SUBSPACE_TO_GLOBAL);
                world.getChunk(entity.getPosition().getX() >> 4, entity.getPosition().getZ() >> 4).addEntity(entity);
            }
        }
    }

    @Override
    public void onEntityRemoved(Entity entityIn) {
        if (entityIn instanceof PhysicsWrapperEntity) {
            ValkyrienSkiesMod.VW_PHYSICS_MANAGER.onShipUnload((PhysicsWrapperEntity) entityIn);
        }
    }

    @Override
    public void broadcastSound(int soundID, BlockPos pos, int data) {
        // TODO Auto-generated method stub

    }

    @Override
    public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
        if (!worldObj.isRemote) {
            for (EntityPlayer entityplayermp : worldObj.playerEntities) {
                if (entityplayermp != null && entityplayermp.getEntityId() != breakerId) {
                    Vector posVector = new Vector(pos.getX(), pos.getY(), pos.getZ());

                    Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysicsObject(worldObj, pos);

                    physicsObject.ifPresent(object -> object
                            .getShipTransformationManager()
                            .getCurrentTickTransform()
                            .transform(posVector,
                                    TransformType.SUBSPACE_TO_GLOBAL));

                    double d0 = posVector.X - entityplayermp.posX;
                    double d1 = posVector.Y - entityplayermp.posY;
                    double d2 = posVector.Z - entityplayermp.posZ;

                    if (d0 * d0 + d1 * d1 + d2 * d2 < 1024.0D) {
                        ((EntityPlayerMP) entityplayermp).connection
                                .sendPacket(new SPacketBlockBreakAnim(breakerId, pos, progress));
                    }
                }
            }
        }
    }

    @Override
    public void spawnParticle(int p_190570_1_, boolean p_190570_2_, boolean p_190570_3_, double p_190570_4_,
                              double p_190570_6_, double p_190570_8_, double p_190570_10_, double p_190570_12_, double p_190570_14_,
                              int... p_190570_16_) {
        // TODO Auto-generated method stub

    }

}
