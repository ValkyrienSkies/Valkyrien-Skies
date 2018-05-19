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

package valkyrienwarfare.mod.physmanagement.interaction;

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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.combat.entity.EntityMountingWeaponBase;
import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physics.data.TransformType;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

public class ValkyrienWarfareWorldEventListener implements IWorldEventListener {

    private World worldObj;

    public ValkyrienWarfareWorldEventListener(World world) {
        worldObj = world;
    }

    // TODO: Maybe replace the ASM setBlockState with this instead
    @Override
    public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(worldObj, pos);
        if (worldObj.isRemote) {
            worldIn.markBlockRangeForRenderUpdate(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(),
                    pos.getZ());
            // Strange bounding box error on CLIENT SIDE Fix, possibly broken and terrible,
            // but probably ok
            if (wrapper != null) {
                // wrapper.wrapping.onSetBlockState(oldState, newState, pos);
            }
        } else {
            if (wrapper != null) {
                // TODO: Find a new way to make this happen
                // wrapper.wrapping.pilotingController.onSetBlockInShip(pos, newState);
            }
        }
    }

    @Override
    public void notifyLightSet(BlockPos pos) {
        // TODO Auto-generated method stub

    }

    @Override
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
        // if(worldObj.isRemote){
        // int midX = (x1 + x2) / 2;
        // int midY = (y1 + y2) / 2;
        // int midZ = (z1 + z2) / 2;
        // BlockPos newPos = new BlockPos(midX, midY, midZ);
        // PhysicsWrapperEntity wrapper =
        // ValkyrienWarfareMod.physicsManager.getObjectManagingPos(worldObj, newPos);
        // if (wrapper != null && wrapper.wrapping.renderer != null) {
        // wrapper.wrapping.renderer.updateRange(x1-1, y1-1, z1-1, x2+1, y2+1, z2+1);
        // }
        // }
    }

    @Override
    public void playSoundToAllNearExcept(EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x,
            double y, double z, float volume, float pitch) {
        // TODO Auto-generated method stub

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
    public void onEntityAdded(Entity entityIn) {
        int oldChunkX = MathHelper.floor(entityIn.posX / 16.0D);
        int oldChunkZ = MathHelper.floor(entityIn.posZ / 16.0D);

        BlockPos posAt = new BlockPos(entityIn);
        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(worldObj, posAt);
        if (!(entityIn instanceof EntityFallingBlock) && wrapper != null && wrapper.wrapping.coordTransform != null) {
            if (entityIn instanceof EntityMountingWeaponBase || entityIn instanceof EntityArmorStand
                    || entityIn instanceof EntityPig || entityIn instanceof EntityBoat) {
                // entity.startRiding(wrapper);
                wrapper.wrapping.fixEntity(entityIn, new Vector(entityIn));
                wrapper.wrapping.queueEntityForMounting(entityIn);
            }
            RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.getCurrentTickTransform(), entityIn, TransformType.LOCAL_TO_GLOBAL);

            int newChunkX = MathHelper.floor(entityIn.posX / 16.0D);
            int newChunkZ = MathHelper.floor(entityIn.posZ / 16.0D);

            worldObj.getChunkFromChunkCoords(oldChunkX, oldChunkZ).removeEntity(entityIn);
            worldObj.getChunkFromChunkCoords(newChunkX, newChunkZ).addEntity(entityIn);

        }
        if (entityIn instanceof PhysicsWrapperEntity) {
            ValkyrienWarfareMod.physicsManager.onShipLoad((PhysicsWrapperEntity) entityIn);
        }

        if (!(entityIn instanceof EntityFallingBlock) && wrapper != null && wrapper.wrapping.coordTransform != null) {
            if (entityIn instanceof EntityMountingWeaponBase || entityIn instanceof EntityArmorStand
                    || entityIn instanceof EntityPig || entityIn instanceof EntityBoat) {
                // entity.startRiding(wrapper);
                wrapper.wrapping.fixEntity(entityIn, new Vector(entityIn));
                wrapper.wrapping.queueEntityForMounting(entityIn);
            }
            RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.getCurrentTickTransform(), entityIn, TransformType.LOCAL_TO_GLOBAL);
        }
    }

    @Override
    public void onEntityRemoved(Entity entityIn) {
        if (entityIn instanceof PhysicsWrapperEntity) {
            ValkyrienWarfareMod.physicsManager.onShipUnload((PhysicsWrapperEntity) entityIn);
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

                    PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(worldObj,
                            pos);

                    if (wrapper != null) {
                        wrapper.wrapping.coordTransform.getCurrentTickTransform().transform(posVector, TransformType.LOCAL_TO_GLOBAL);
//                        RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, posVector);
                    }

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
