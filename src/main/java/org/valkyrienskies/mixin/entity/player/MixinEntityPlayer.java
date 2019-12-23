package org.valkyrienskies.mixin.entity.player;

import java.util.Optional;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.addon.control.piloting.ControllerInputType;
import org.valkyrienskies.addon.control.piloting.IShipPilot;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physmanagement.shipdata.ShipData;
import org.valkyrienskies.mod.common.physmanagement.shipdata.ShipPositionData;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

/**
 * Todo: Delete preGetBedSpawnLocation and turn IShipPilot into a capability.
 */
@Deprecated
@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase implements IShipPilot {

    public PhysicsWrapperEntity pilotedShip;
    public BlockPos blockBeingControlled;
    public ControllerInputType controlInputType;

    // Constructor doesn't do anything, just here because java wont compile if it
    // wasn't.
    public MixinEntityPlayer() {
        super(null);
    }

    @Inject(method = "getBedSpawnLocation", at = @At("HEAD"), cancellable = true)
    private static void preGetBedSpawnLocation(World worldIn, BlockPos bedLocation,
        boolean forceSpawn,
        CallbackInfoReturnable<BlockPos> callbackInfo){
        int chunkX = bedLocation.getX() >> 4;
        int chunkZ = bedLocation.getZ() >> 4;

        Optional<ShipData> shipData = ValkyrienUtils.getQueryableData(worldIn).getShipFromChunk(chunkX, chunkZ);

        if (shipData.isPresent()) {
            ShipPositionData positionData = shipData.get().getPositionData();

            if (positionData != null) {
                Vector bedPositionInWorld = new Vector(bedLocation.getX() + .5D,
                        bedLocation.getY() + .5D, bedLocation.getZ() + .5D);
                positionData.getTransform()
                        .transform(bedPositionInWorld, TransformType.SUBSPACE_TO_GLOBAL);
                bedPositionInWorld.Y += 1D;
                bedLocation = new BlockPos(bedPositionInWorld.X, bedPositionInWorld.Y,
                        bedPositionInWorld.Z);

                callbackInfo.setReturnValue(bedLocation);
            } else {
                System.err.println(
                        "A ship just had chunks claimed persistent, but not any position data persistent");
            }
        }
    }

    @Override
    public PhysicsWrapperEntity getPilotedShip() {
        return pilotedShip;
    }

    @Override
    public void setPilotedShip(PhysicsWrapperEntity wrapper) {
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
