package org.valkyrienskies.mixin.client.renderer;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.valkyrienskies.addon.control.piloting.IShipPilot;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physmanagement.interaction.IWorldVS;
import org.valkyrienskies.mod.common.util.EntityShipMountData;
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

    protected final Vector eyeVector = new Vector();
    protected final Vector cachedPosition = new Vector();
    protected EntityShipMountData mountData;
    protected float vs_partialTicks;

    @Shadow
    @Final
    public Minecraft mc;

    @Shadow
    public float thirdPersonDistancePrev;

    @Shadow
    public boolean cloudFog;

    @Redirect(
            method = "Lnet/minecraft/client/renderer/EntityRenderer;orientCamera(F)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;getRenderViewEntity()Lnet/minecraft/entity/Entity;"
            ))
    private Entity resetThingsBeforeOrientCamera(Minecraft mc, float partialTicks) {
        Entity entity = mc.getRenderViewEntity();

        { //set up state
            this.vs_partialTicks = partialTicks;
            EntityShipMountData mountData = ValkyrienUtils.getMountedShipAndPos(entity);
            this.mountData = mountData.isMounted() ? mountData : null;
            this.eyeVector.setValue(0.0d, entity.getEyeHeight() + (
                    entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPlayerSleeping()
                            ? 0.7d : 0.0d), 0.0d);
        }

        if (this.mountData != null) {

            mountData.getMountedShip().getShipTransformationManager().getRenderTransform()
                .rotate(this.eyeVector, TransformType.SUBSPACE_TO_GLOBAL);

            this.cachedPosition.setValue(this.mountData.getMountPos());
            this.mountData.getMountedShip().getShipTransformationManager().getRenderTransform()
                    .transform(this.cachedPosition, TransformType.SUBSPACE_TO_GLOBAL);
        }

        return entity;
    }

    @Redirect(
            method = "Lnet/minecraft/client/renderer/EntityRenderer;orientCamera(F)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;getEyeHeight()F"
            ))
    private float dontGetEyeHeight(Entity e) {
        return 0.0f;
    }

    @ModifyVariable(
            method = "Lnet/minecraft/client/renderer/EntityRenderer;orientCamera(F)V",
            index = 4,
            at = @At(
                    value = "JUMP",
                    opcode = Opcodes.IFEQ,
                    ordinal = 0,
                    shift = At.Shift.BY,
                    by = -2
            ))
    private double offsetXIfMounted(double oldVal) {
        return (this.mountData != null
                ? this.cachedPosition.x : oldVal) + this.eyeVector.x;
    }

    @ModifyVariable(
            method = "Lnet/minecraft/client/renderer/EntityRenderer;orientCamera(F)V",
            index = 6,
            at = @At(
                    value = "JUMP",
                    opcode = Opcodes.IFEQ,
                    ordinal = 0,
                    shift = At.Shift.BY,
                    by = -2
            ))
    private double offsetYIfMounted(double oldVal) {
        return (this.mountData != null
                ? this.cachedPosition.y : oldVal) + this.eyeVector.y;
    }

    @ModifyVariable(
            method = "Lnet/minecraft/client/renderer/EntityRenderer;orientCamera(F)V",
            index = 8,
            at = @At(
                    value = "JUMP",
                    opcode = Opcodes.IFEQ,
                    ordinal = 0,
                    shift = At.Shift.BY,
                    by = -2
            ))
    private double offsetZIfMounted(double oldVal) {
        return (this.mountData != null
                ? this.cachedPosition.z : oldVal) + this.eyeVector.z;
    }

    @ModifyConstant(
            method = "Lnet/minecraft/client/renderer/EntityRenderer;orientCamera(F)V",
            constant = @Constant(
                    doubleValue = 1.0d,
                    ordinal = 0
            ))
    private double dontIncrementF(double one) {
        return 0.0d;
    }

    @Redirect(
            method = "Lnet/minecraft/client/renderer/EntityRenderer;orientCamera(F)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GlStateManager;translate(FFF)V",
                    ordinal = 0
            ))
    private void dontDoTranslate1(float x, float y, float z) {
    }

    @Redirect(
            method = "Lnet/minecraft/client/renderer/EntityRenderer;orientCamera(F)V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/settings/GameSettings;debugCamEnable:Z",
                    ordinal = 0
            ))
    private boolean dontRotateIfMounted(GameSettings settings, float partialTicks) {
        if (!settings.debugCamEnable) {
            Entity entity = this.mc.getRenderViewEntity();

            if (this.mountData != null) {
                Vector playerPosInLocal = new Vector(this.mountData.getMountPos());

                playerPosInLocal.subtract(0.5d, 0.6875d, 0.5d);
                playerPosInLocal.roundToWhole();

                BlockPos bedPos = new BlockPos(playerPosInLocal.x, playerPosInLocal.y,
                        playerPosInLocal.z);
                IBlockState state = this.mc.world.getBlockState(bedPos);

                Block block = state.getBlock();
                float angleYaw = 0.0f;

                if (block != null && block.isBed(state, entity.world, bedPos, entity)) {
                    angleYaw =
                            block.getBedDirection(state, entity.world, bedPos).getHorizontalIndex()
                                    * 90.0f;
                    angleYaw += 180.0f;
                }

                entity.rotationYaw = entity.prevRotationYaw = angleYaw;
                entity.rotationPitch = entity.prevRotationPitch = 0.0f;
            } else {
                //this will cause vanilla code to be executed
                return true;
            }
        }
        return false;
    }

    @ModifyVariable(
            method = "Lnet/minecraft/client/renderer/EntityRenderer;orientCamera(F)V",
            index = 10,
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/settings/GameSettings;debugCamEnable:Z",
                    ordinal = 1
            ))
    private double zoomOutIfPiloting(double oldZoom) {
        //TODO: Make this number scale with the Ship
        return ((IShipPilot) this.mc.player).isPilotingShip() ? 15.0d : oldZoom;
    }

    @Redirect(
            method = "Lnet/minecraft/client/renderer/EntityRenderer;orientCamera(F)V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/settings/GameSettings;thirdPersonView:I",
                    ordinal = 1
            ))
    private int excludeShipFromRayTracerBeforeDepthProbe(GameSettings settings) {
        ((IWorldVS) this.mc.world)
                .excludeShipFromRayTracer(((IShipPilot) this.mc.player).getPilotedShip());

        return settings.thirdPersonView;
    }

    //8 ray traces come here

    @Redirect(
            method = "Lnet/minecraft/client/renderer/EntityRenderer;orientCamera(F)V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/settings/GameSettings;thirdPersonView:I",
                    ordinal = 2
            ))
    private int unexcludeShipFromRayTracerAfterDepthProbe(GameSettings settings) {
        ((IWorldVS) this.mc.world)
                .unexcludeShipFromRayTracer(((IShipPilot) this.mc.player).getPilotedShip());

        return settings.thirdPersonView;
    }

    @Redirect(
            method = "Lnet/minecraft/client/renderer/EntityRenderer;orientCamera(F)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GlStateManager;translate(FFF)V",
                    ordinal = 4
            ))
    private void rotateCameraAndFixFinalTranslate(float x, float y, float z, float partialTicks) {
        if (this.mountData != null
                && this.mountData.getMountedShip().getShipRenderer() != null) {

            Quaterniondc orientationQuat = mountData.getMountedShip().getShipTransformationManager()
                .getRenderTransform().rotationQuaternion(TransformType.SUBSPACE_TO_GLOBAL);

            Vector3dc angles = orientationQuat.getEulerAnglesXYZ(new Vector3d());

            float moddedPitch = (float) Math.toDegrees(angles.x());
            float moddedYaw = (float) Math.toDegrees(angles.y());
            float moddedRoll = (float) Math.toDegrees(angles.z());

            GlStateManager.rotate(-moddedRoll, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(-moddedYaw, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-moddedPitch, 1.0F, 0.0F, 0.0F);
        }

        //reset
        this.mountData = null;

        GlStateManager.translate(x - this.eyeVector.x, y - this.eyeVector.y, z - this.eyeVector.z);
    }


    @Redirect(
            method = "Lnet/minecraft/client/renderer/EntityRenderer;orientCamera(F)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderGlobal;hasCloudFog(DDDF)Z"
            ))
    private boolean fixHasCloudFogCoordinates(RenderGlobal renderGlobal, double x, double y,
                                              double z, float partialTicks) {
        return renderGlobal
                .hasCloudFog(x + this.eyeVector.x, y + this.eyeVector.y, z + this.eyeVector.z,
                        partialTicks);
    }

    /**
     * @author thebest108
     */
    /*@Overwrite
    public void orientCamera(float partialTicks) {
        Entity entity = this.mc.getRenderViewEntity();

        Vector eyeVector = new Vector(0, entity.getEyeHeight(), 0);

        if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPlayerSleeping()) {
            eyeVector.Y += .7D;
        }

        double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks;
        double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks;
        double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks;

        EntityShipMountData mountData = ValkyrienUtils.getMountedShipAndPos(entity);
        // Probably overkill, but this should 100% fix the crash in issue #78
        if (mountData.isMounted() && mountData.getMountedShip()
                .getShipRenderer().offsetPos != null) {
            Quaternion orientationQuat = mountData.getMountedShip()
                    .getShipRenderer()
                    .getSmoothRotationQuat(partialTicks);

            double[] radians = orientationQuat.toRadians();

            float moddedPitch = (float) Math.toDegrees(radians[0]);
            float moddedYaw = (float) Math.toDegrees(radians[1]);
            float moddedRoll = (float) Math.toDegrees(radians[2]);

            double[] orientationMatrix = RotationMatrices.getRotationMatrix(moddedPitch, moddedYaw, moddedRoll);

            RotationMatrices.applyTransform(orientationMatrix, eyeVector);

            Vector playerPosition = new Vector(mountData.getMountPos());

            //            RotationMatrices.applyTransform(fixedOnto.wrapping.coordTransform.RlToWTransform, playerPosition);

            mountData.getMountedShip()
                    .getShipTransformationManager()
                    .getRenderTransform()
                    .transform(playerPosition, TransformType.SUBSPACE_TO_GLOBAL);

            d0 = playerPosition.X;
            d1 = playerPosition.Y;
            d2 = playerPosition.Z;

            //            entity.posX = entity.prevPosX = entity.lastTickPosX = d0;
            //            entity.posY = entity.prevPosY = entity.lastTickPosY = d1;
            //            entity.posZ = entity.prevPosZ = entity.lastTickPosZ = d2;
        }

        d0 += eyeVector.X;
        d1 += eyeVector.Y;
        d2 += eyeVector.Z;

        if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPlayerSleeping()) {
            //            f = (float)((double)f + 1.0D);
            //            GlStateManager.translate(0.0F, 0.3F, 0.0F);

            if (!this.mc.gameSettings.debugCamEnable) {
                //VS code starts here
                if (mountData.isMounted()) {
                    Vector playerPosInLocal = new Vector(mountData.getMountPos());

                    playerPosInLocal.subtract(.5D, .6875, .5);
                    playerPosInLocal.roundToWhole();

                    BlockPos bedPos = new BlockPos(playerPosInLocal.X, playerPosInLocal.Y, playerPosInLocal.Z);
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

                    RayTraceResult raytraceresult = EntityMoveInjectionMethods.rayTraceBlocksIgnoreShip(Minecraft.getMinecraft().world, new Vec3d(d0 + f3, d1 + f4, d2 + f5), new Vec3d(d0 - d4 + f3 + f5, d1 - d6 + f4, d2 - d5 + f5), false, false, false, pilot.getPilotedShip());
                    //                    renderer.mc.theWorld.rayTraceBlocks(new Vec3d(d0 + (double)f3, d1 + (double)f4, d2 + (double)f5), new Vec3d(d0 - d4 + (double)f3 + (double)f5, d1 - d6 + (double)f4, d2 - d5 + (double)f5));

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
            Quaternion orientationQuat = mountData.getMountedShip()
                    .getShipRenderer()
                    .getSmoothRotationQuat(partialTicks);

            double[] radians = orientationQuat.toRadians();

            float moddedPitch = (float) Math.toDegrees(radians[0]);
            float moddedYaw = (float) Math.toDegrees(radians[1]);
            float moddedRoll = (float) Math.toDegrees(radians[2]);

            GlStateManager.rotate(-moddedRoll, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(-moddedYaw, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-moddedPitch, 1.0F, 0.0F, 0.0F);
        }


        GlStateManager.translate(-eyeVector.X, -eyeVector.Y, -eyeVector.Z);
        d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks + eyeVector.X;
        d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks + eyeVector.Y;
        d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks + eyeVector.Z;
        this.cloudFog = this.mc.renderGlobal.hasCloudFog(d0, d1, d2, partialTicks);
    }*/

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
