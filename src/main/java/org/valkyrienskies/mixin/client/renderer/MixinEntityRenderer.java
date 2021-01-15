package org.valkyrienskies.mixin.client.renderer;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.lwjgl.util.vector.Quaternion;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.common.piloting.IShipPilot;
import org.valkyrienskies.mod.common.ships.entity_interaction.EntityMoveInjectionMethods;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransform;
import org.valkyrienskies.mod.common.ships.ship_world.IWorldVS;
import org.valkyrienskies.mod.common.ships.entity_interaction.EntityShipMountData;
import org.valkyrienskies.mod.common.util.JOML;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

/**
 * This used to be one giant overwrite, and has now been cleaned up to be a mess of various mixins.
 * <p>
 * This uses member variables to store things that would be local and can't be due to the fact that
 * we're not using an overwrite, however it shouldn't be an issue since rendering is single-threaded
 * anyway.
 *
 * @author DaPorkchop_
 */
@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Shadow
    @Final
    public Minecraft mc;

    @Shadow
    public float thirdPersonDistancePrev;

    @Shadow
    public boolean cloudFog;

    @Inject(method = "orientCamera", at = @At("HEAD"), cancellable = true)
    private void orientCamera(float partialTicks, CallbackInfo ci) {
        EntityShipMountData mountData = ValkyrienUtils.getMountedShipAndPos(mc.getRenderViewEntity());
        if (mountData.getMountedShip() == null) {
            // Do nothing. We don't want to mess with camera code unless we have to.
            return;
        } else {
            // Take over the camera orientation entirely. Don't let anything else touch it.
            ci.cancel();
        }
        Entity entity = this.mc.getRenderViewEntity();

        Vector3d eyeVector = new Vector3d(0, entity.getEyeHeight(), 0);

        if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPlayerSleeping()) {
            eyeVector.y += .7D;
        }

        double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks;
        double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks;
        double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks;

        // Probably overkill, but this should 100% fix the crash in issue #78
        if (mountData.isMounted() && mountData.getMountedShip()
                .getShipRenderer().offsetPos != null) {
            final ShipTransform renderTransform = mountData.getMountedShip().getShipTransformationManager().getRenderTransform();

            renderTransform.transformDirection(eyeVector, TransformType.SUBSPACE_TO_GLOBAL);

            Vector3d playerPosition = JOML.convert(mountData.getMountPos());

            renderTransform.transformPosition(playerPosition, TransformType.SUBSPACE_TO_GLOBAL);

            d0 = playerPosition.x;
            d1 = playerPosition.y;
            d2 = playerPosition.z;
        }

        d0 += eyeVector.x;
        d1 += eyeVector.y;
        d2 += eyeVector.z;

        if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPlayerSleeping()) {
            //            f = (float)((double)f + 1.0D);
            //            GlStateManager.translate(0.0F, 0.3F, 0.0F);

            if (!this.mc.gameSettings.debugCamEnable) {
                //VS code starts here
                if (mountData.isMounted()) {
                    Vector3d playerPosInLocal = JOML.convert(mountData.getMountPos());

                    playerPosInLocal.sub(.5D, .6875, .5);
                    playerPosInLocal.round();

                    BlockPos bedPos = new BlockPos(playerPosInLocal.x, playerPosInLocal.y, playerPosInLocal.z);
                    IBlockState state = this.mc.world.getBlockState(bedPos);

                    Block block = state.getBlock();

                    float angleYaw = 0;

                    if (block != null && block.isBed(state, entity.world, bedPos, entity)) {
                        angleYaw = block.getBedDirection(state, entity.world, bedPos)
                                .getHorizontalIndex() * 90;
                        angleYaw += 180;
                    }

                    entity.rotationYaw = entity.prevRotationYaw = angleYaw;

                    entity.rotationPitch = entity.prevRotationPitch = 0;

                } else {
                    BlockPos blockpos = new BlockPos(entity);
                    IBlockState iblockstate = this.mc.world.getBlockState(blockpos);

                    net.minecraftforge.client.ForgeHooksClient.orientBedCamera(this.mc.world, blockpos, iblockstate, entity);
                    GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F, 0.0F, -1.0F, 0.0F);
                    GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, -1.0F, 0.0F, 0.0F);
                }

            }
        } else if (this.mc.gameSettings.thirdPersonView > 0) {
            double d3 = this.thirdPersonDistancePrev + (4.0F - this.thirdPersonDistancePrev) * partialTicks;

            IShipPilot shipPilot = (IShipPilot) Minecraft.getMinecraft().player;

            if (shipPilot.isPilotingShip()) {
                //TODO: Make this number scale with the Ship
                d3 = 15D;
            }

            if (this.mc.gameSettings.debugCamEnable) {
                GlStateManager.translate(0.0F, 0.0F, (float) (-d3));
            } else {
                float f1 = entity.rotationYaw;
                float f2 = entity.rotationPitch;

                if (this.mc.gameSettings.thirdPersonView == 2) {
                    f2 += 180.0F;
                }

                double d4 = -MathHelper.sin(f1 * 0.017453292F) * MathHelper.cos(f2 * 0.017453292F) * d3;
                double d5 = MathHelper.cos(f1 * 0.017453292F) * MathHelper.cos(f2 * 0.017453292F) * d3;
                double d6 = (-MathHelper.sin(f2 * 0.017453292F)) * d3;

                for (int i = 0; i < 8; ++i) {
                    float f3 = (i & 1) * 2 - 1;
                    float f4 = (i >> 1 & 1) * 2 - 1;
                    float f5 = (i >> 2 & 1) * 2 - 1;
                    f3 = f3 * 0.1F;
                    f4 = f4 * 0.1F;
                    f5 = f5 * 0.1F;

                    IShipPilot pilot = (IShipPilot) Minecraft.getMinecraft().player;

                    ((IWorldVS) this.mc.world)
                            .excludeShipFromRayTracer(((IShipPilot) this.mc.player).getPilotedShip());

                    // RayTraceResult raytraceresult = EntityMoveInjectionMethods.rayTraceBlocksIgnoreShip(Minecraft.getMinecraft().world, new Vec3d(d0 + f3, d1 + f4, d2 + f5), new Vec3d(d0 - d4 + f3 + f5, d1 - d6 + f4, d2 - d5 + f5), false, false, false, pilot.getPilotedShip());
                    RayTraceResult raytraceresult = mc.world.rayTraceBlocks(new Vec3d(d0 + (double)f3, d1 + (double)f4, d2 + (double)f5), new Vec3d(d0 - d4 + (double)f3 + (double)f5, d1 - d6 + (double)f4, d2 - d5 + (double)f5));

                    ((IWorldVS) this.mc.world)
                            .unexcludeShipFromRayTracer(((IShipPilot) this.mc.player).getPilotedShip());

                    if (raytraceresult != null) {
                        double d7 = raytraceresult.hitVec.distanceTo(new Vec3d(d0, d1, d2));

                        if (d7 < d3) {
                            d3 = d7;
                        }
                    }
                }

                if (this.mc.gameSettings.thirdPersonView == 2) {
                    GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                }

                GlStateManager.rotate(entity.rotationPitch - f2, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(entity.rotationYaw - f1, 0.0F, 1.0F, 0.0F);
                GlStateManager.translate(0.0F, 0.0F, (float) (-d3));
                GlStateManager.rotate(f1 - entity.rotationYaw, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(f2 - entity.rotationPitch, 1.0F, 0.0F, 0.0F);
            }
        } else {
            GlStateManager.translate(0.0F, 0.0F, 0.05F);
        }

        if (!this.mc.gameSettings.debugCamEnable) {
            float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F;
            float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
            float roll = 0.0F;
            if (entity instanceof EntityAnimal) {
                EntityAnimal entityanimal = (EntityAnimal) entity;
                yaw = entityanimal.prevRotationYawHead + (entityanimal.rotationYawHead - entityanimal.prevRotationYawHead) * partialTicks + 180.0F;
            }
            IBlockState state = ActiveRenderInfo.getBlockStateAtEntityViewpoint(this.mc.world, entity, partialTicks);
            net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup event = new net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup(EntityRenderer.class.cast(this), entity, state, partialTicks, yaw, pitch, roll);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
            GlStateManager.rotate(event.getRoll(), 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(event.getPitch(), 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(event.getYaw(), 0.0F, 1.0F, 0.0F);
        }

        if (mountData.isMounted() && mountData.getMountedShip()
                .getShipRenderer().offsetPos != null) {

            final ShipTransform renderTransform = mountData.getMountedShip().getShipTransformationManager().getRenderTransform();

            Quaterniond orientationQuat = renderTransform.rotationQuaternion(TransformType.SUBSPACE_TO_GLOBAL);

            Vector3dc radians = orientationQuat.getEulerAnglesXYZ(new Vector3d());

            float moddedPitch = (float) Math.toDegrees(radians.x());
            float moddedYaw = (float) Math.toDegrees(radians.y());
            float moddedRoll = (float) Math.toDegrees(radians.z());

            GlStateManager.rotate(-moddedRoll, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(-moddedYaw, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-moddedPitch, 1.0F, 0.0F, 0.0F);
        }


        GlStateManager.translate(-eyeVector.x, -eyeVector.y, -eyeVector.z);
        d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks + eyeVector.x;
        d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks + eyeVector.y;
        d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks + eyeVector.z;
        this.cloudFog = this.mc.renderGlobal.hasCloudFog(d0, d1, d2, partialTicks);
    }

    //below is the local variable table for orientCamera
    /*****************************************************************************************************************/
    /*         Target Class : net.minecraft.client.renderer.EntityRenderer                                           */
    /*        Target Method : orientCamera                                                                           */
    /*        Callback Name : localvar$zoomOutIfPiloting$zzo000                                                      */
    /*         Capture Type : double                                                                                 */
    /*          Instruction : FieldInsnNode GETFIELD                                                                 */
    /*****************************************************************************************************************/
    /*           Match mode : IMPLICIT (match single)                                                                */
    /*        Match ordinal : any                                                                                    */
    /*          Match index : any                                                                                    */
    /*        Match name(s) : any                                                                                    */
    /*            Args only : false                                                                                  */
    /*****************************************************************************************************************/
    /* INDEX  ORDINAL                            TYPE  NAME                                                CANDIDATE */
    /* [  1]    [  0]                           float  partialTicks                                        -         */
    /* [  2]    [  0]                          Entity  entity                                              -         */
    /* [  3]    [  1]                           float  f                                                   -         */
    /* [  4]    [  0]                          double  d0                                                  YES       */
    /* [  5]                                    <top>                                                                */
    /* [  6]    [  1]                          double  d1                                                  YES       */
    /* [  7]                                    <top>                                                                */
    /* [  8]    [  2]                          double  d2                                                  YES       */
    /* [  9]                                    <top>                                                                */
    /* [ 10]    [  3]                          double  d3                                                  YES       */
    /* [ 11]    [  0]                     IBlockState  var11                                               -         */
    /* [ 12]                                        -                                                                */
    /* [ 13]                                        -                                                                */
    /* [ 14]                                        -                                                                */
    /* [ 15]                                        -                                                                */
    /* [ 16]                                        -                                                                */
    /* [ 17]                                        -                                                                */
    /* [ 18]                                        -                                                                */
    /* [ 19]                                        -                                                                */
    /* [ 20]                                        -                                                                */
    /* [ 21]                                        -                                                                */
    /* [ 22]                                        -                                                                */
    /* [ 23]                                        -                                                                */
    /* [ 24]                                        -                                                                */
    /* [ 25]                                        -                                                                */
    /* [ 26]                                        -                                                                */
    /* [ 27]    [  1]                          Entity  var27                                               -         */
    /* [ 28]                                        -                                                                */
    /* [ 29]                                        -                                                                */
    /* [ 30]                                        -                                                                */
    /* [ 31]                                        -                                                                */
    /*****************************************************************************************************************/
}
