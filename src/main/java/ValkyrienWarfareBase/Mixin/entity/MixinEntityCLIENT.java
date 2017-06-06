package ValkyrienWarfareBase.Mixin.entity;

import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntityCLIENT implements ICommandSender, net.minecraftforge.common.capabilities.ICapabilitySerializable<NBTTagCompound> {

    @Shadow
    public double posX;

    @Shadow
    public double posY;

    @Shadow
    public double posZ;

    @Shadow
    public double prevPosX;

    @Shadow
    public double prevPosY;

    @Shadow
    public double prevPosZ;

    @Shadow
    public float getEyeHeight() { return 0.0f; }

    @Inject(method = "getPositionEyes(F)L", at = @At("HEAD"), cancellable = true)
    public void getPositionEyesInject(float partialTicks, CallbackInfoReturnable<Vec3d> callbackInfo){
        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getShipFixedOnto(Entity.class.cast(this));

        if(wrapper != null){
            Vector playerPosition = new Vector(wrapper.wrapping.getLocalPositionForEntity(Entity.class.cast(this)));

            RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.RlToWTransform, playerPosition);

            Vector playerEyes = new Vector(0, this.getEyeHeight(), 0);
            //Remove the original position added for the player's eyes
            RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWRotation, playerEyes);
            //Add the new rotate player eyes to the position
            playerPosition.add(playerEyes);
            callbackInfo.setReturnValue(playerPosition.toVec3d());
            callbackInfo.cancel(); //return the value, as opposed to the default one
        }
    }
}
