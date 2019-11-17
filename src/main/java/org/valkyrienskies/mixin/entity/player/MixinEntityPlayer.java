/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2019 the Valkyrien Skies team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Skies team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Skies team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package org.valkyrienskies.mixin.entity.player;

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

import java.util.Optional;

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
                positionData.transform()
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
