package org.valkyrienskies.mixin.client.renderer.entity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.util.EntityShipMountData;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

@Mixin(RenderManager.class)
public abstract class MixinRenderManager {

    private boolean hasChanged = false;

    @Shadow
    public abstract void renderEntity(Entity entityIn, double x, double y, double z, float yaw,
        float partialTicks, boolean p_188391_10_);

    @Inject(method = "renderEntity",
        at = @At("HEAD"),
        cancellable = true)
    public void preDoRenderEntity(Entity entityIn, double x, double y, double z, float yaw,
        float partialTicks, boolean p_188391_10_, CallbackInfo callbackInfo) {
        if (!hasChanged) {
            EntityShipMountData mountData = ValkyrienUtils.getMountedShipAndPos(entityIn);

            if (mountData.isMounted()) {
                double oldPosX = entityIn.posX;
                double oldPosY = entityIn.posY;
                double oldPosZ = entityIn.posZ;

                double oldLastPosX = entityIn.lastTickPosX;
                double oldLastPosY = entityIn.lastTickPosY;
                double oldLastPosZ = entityIn.lastTickPosZ;

                Vector localPosition = new Vector(mountData.getMountPos());

                mountData.getMountedShip()
                    .getShipRenderer()
                    .applyRenderTransform(partialTicks);

                if (localPosition != null) {
                    localPosition = new Vector(localPosition);

                    localPosition.X -= mountData.getMountedShip()
                        .getShipRenderer().offsetPos.getX();
                    localPosition.Y -= mountData.getMountedShip()
                        .getShipRenderer().offsetPos.getY();
                    localPosition.Z -= mountData.getMountedShip()
                        .getShipRenderer().offsetPos.getZ();

                    x = entityIn.posX = entityIn.lastTickPosX = localPosition.X;
                    y = entityIn.posY = entityIn.lastTickPosY = localPosition.Y;
                    z = entityIn.posZ = entityIn.lastTickPosZ = localPosition.Z;

                }

                boolean makePlayerMount = false;
                PhysicsWrapperEntity shipRidden = null;

                if (entityIn instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entityIn;
                    if (player.isPlayerSleeping()) {
                        if (player.ridingEntity instanceof PhysicsWrapperEntity) {
                            shipRidden = (PhysicsWrapperEntity) player.ridingEntity;
                        }
//                    shipRidden = ValkyrienSkiesMod.physicsManager.getShipFixedOnto(entityIn);

                        if (shipRidden != null) {
                            player.ridingEntity = null;
                            makePlayerMount = true;

                            //Now fix the rotation of sleeping players
                            Vector playerPosInLocal = new Vector(mountData.getMountPos());

                            playerPosInLocal.subtract(.5D, .6875, .5);
                            playerPosInLocal.roundToWhole();

                            BlockPos bedPos = new BlockPos(playerPosInLocal.X, playerPosInLocal.Y,
                                playerPosInLocal.Z);
                            IBlockState state = entityIn.world.getBlockState(bedPos);

                            Block block = state.getBlock();

                            float angleYaw = 0;

//                        player.setRenderOffsetForSleep(EnumFacing.SOUTH);

                            if (block != null && block
                                .isBed(state, entityIn.world, bedPos, entityIn)) {
                                angleYaw = (float) (
                                    block.getBedDirection(state, entityIn.world, bedPos)
                                        .getHorizontalIndex() * 90);
//                            angleYaw += 180;
                            }
                            GL11.glRotatef(angleYaw, 0, 1F, 0);
                        }
                    }
                }

                hasChanged = true;
                this.renderEntity(entityIn, x, y, z, yaw, partialTicks, p_188391_10_);
                hasChanged = false;

                if (makePlayerMount) {
                    EntityPlayer player = (EntityPlayer) entityIn;

                    player.ridingEntity = shipRidden;
                }

                if (localPosition != null) {
                    mountData.getMountedShip()
                        .getShipRenderer()
                        .inverseTransform(partialTicks);
                }

                entityIn.posX = oldPosX;
                entityIn.posY = oldPosY;
                entityIn.posZ = oldPosZ;

                entityIn.lastTickPosX = oldLastPosX;
                entityIn.lastTickPosY = oldLastPosY;
                entityIn.lastTickPosZ = oldLastPosZ;

                callbackInfo.cancel();
            }
        }
    }
}
