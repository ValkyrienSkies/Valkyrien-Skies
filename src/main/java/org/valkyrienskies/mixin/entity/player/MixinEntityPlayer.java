package org.valkyrienskies.mixin.entity.player;

import java.util.Optional;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.addon.control.piloting.ControllerInputType;
import org.valkyrienskies.addon.control.piloting.IShipPilot;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.physics.management.physo.ShipData;
import org.valkyrienskies.mod.common.util.JOML;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

/**
 * Todo: Delete preGetBedSpawnLocation and turn IShipPilot into a capability.
 */
@Deprecated
@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase implements IShipPilot {

    @Shadow public BlockPos bedLocation;
    private PhysicsObject pilotedShip;
    private BlockPos blockBeingControlled;
    private ControllerInputType controlInputType;

    // Constructor doesn't do anything, just here because java wont compile if it
    // wasn't.
    public MixinEntityPlayer() {
        super(null);
    }

    @Inject(method = "getBedSpawnLocation", at = @At("HEAD"), cancellable = true)
    private static void preGetBedSpawnLocation(World worldIn, BlockPos bedLocation,
        boolean forceSpawn,
        CallbackInfoReturnable<BlockPos> callbackInfo){

        Optional<ShipData> shipData = ValkyrienUtils.getQueryableData(worldIn)
            .getShipFromBlock(bedLocation);

        if (shipData.isPresent()) {
            ShipTransform positionData = shipData.get().getShipTransform();

            if (positionData != null) {
                Vector3d bedLocationD = JOML.castDouble(JOML.convert(bedLocation))
                    .add(0.5, 0.5, 0.5);
                positionData.getSubspaceToGlobal().transformPosition(bedLocationD);
                bedLocationD.y += 1D;
                bedLocation = JOML.toMinecraft(JOML.castInt(bedLocationD));

                callbackInfo.setReturnValue(bedLocation);
            } else {
                System.err.println(
                        "A ship just had chunks claimed persistent, but not any position data persistent");
            }
        }
    }

    @Override
    public PhysicsObject getPilotedShip() {
        return pilotedShip;
    }

    @Override
    public void setPilotedShip(PhysicsObject wrapper) {
        pilotedShip = wrapper;
    }

    @Override
    public boolean isPilotingShip() {
        return pilotedShip != null;
    }

    @Override
    public BlockPos getPosBeingControlled() {
        return blockBeingControlled;
    }

    @Override
    public void setPosBeingControlled(BlockPos pos) {
        blockBeingControlled = pos;
    }

    @Override
    public ControllerInputType getControllerInputEnum() {
        return controlInputType;
    }

    @Override
    public void setControllerInputEnum(ControllerInputType type) {
        controlInputType = type;
    }

    @Override
    public boolean isPilotingATile() {
        return blockBeingControlled != null;
    }

    @Override
    public boolean isPiloting() {
        return isPilotingShip() || isPilotingATile();
    }

    @Override
    public void stopPilotingEverything() {
        setPilotedShip(null);
        setPosBeingControlled(null);
        setControllerInputEnum(null);
    }
}
