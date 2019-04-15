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

package valkyrienwarfare.addon.control.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;
import valkyrienwarfare.addon.control.block.BlockShipPilotsChair;
import valkyrienwarfare.addon.control.piloting.ControllerInputType;
import valkyrienwarfare.addon.control.piloting.PilotControlsMessage;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.math.RotationMatrices;
import valkyrienwarfare.math.Vector;
import valkyrienwarfare.physics.management.PhysicsObject;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

public class TileEntityPilotsChair extends ImplTileEntityPilotable {

    @Override
    void processControlMessage(PilotControlsMessage message, EntityPlayerMP sender) {
        IBlockState blockState = getWorld().getBlockState(getPos());
        if (blockState.getBlock() == ValkyrienWarfareControl.INSTANCE.vwControlBlocks.pilotsChair) {
            PhysicsWrapperEntity wrapper = getParentPhysicsEntity();
            if (wrapper != null) {
                processCalculationsForControlMessageAndApplyCalculations(wrapper, message, blockState);
            }
        } else {
            setPilotEntity(null);
        }
    }

    @Override
    final ControllerInputType getControlInputType() {
        return ControllerInputType.PilotsChair;
    }

    @Override
    boolean setClientPilotingEntireShip() {
        return true;
    }

    @Override
    public final void onStartTileUsage(EntityPlayer player) {
        getParentPhysicsEntity().getPhysicsObject().getPhysicsProcessor().actAsArchimedes = true;
    }

    @Override
    public final void onStopTileUsage() {
        getParentPhysicsEntity().getPhysicsObject().getPhysicsProcessor().actAsArchimedes = false;
    }

    private final void processCalculationsForControlMessageAndApplyCalculations(PhysicsWrapperEntity wrapper, PilotControlsMessage message, IBlockState state) {
        BlockPos chairPosition = getPos();
        PhysicsObject controlledShip = wrapper.getPhysicsObject();

        double pilotPitch = 0D;
        double pilotYaw = ((BlockShipPilotsChair) state.getBlock()).getChairYaw(state, chairPosition);
        double pilotRoll = 0D;

        double[] pilotRotationMatrix = RotationMatrices.getRotationMatrix(pilotPitch, pilotYaw, pilotRoll);

        Vector playerDirection = new Vector(1, 0, 0);

        RotationMatrices.applyTransform(pilotRotationMatrix, playerDirection);

        Vector upDirection = new Vector(0, 1, 0);

        Vector downDirection = new Vector(0, -1, 0);

        Vector idealAngularDirection = new Vector();

        Vector idealLinearVelocity = new Vector();

        Vector shipUp = new Vector(0, 1, 0);
        Vector shipUpPosIdeal = new Vector(0, 1, 0);

        if (message.airshipForward_KeyDown) {
            idealLinearVelocity.add(playerDirection);
        }
        if (message.airshipBackward_KeyDown) {
            idealLinearVelocity.subtract(playerDirection);
        }

        controlledShip.getShipTransformationManager().getCurrentTickTransform().rotate(idealLinearVelocity, TransformType.SUBSPACE_TO_GLOBAL);
        controlledShip.getShipTransformationManager().getCurrentTickTransform().rotate(shipUp, TransformType.SUBSPACE_TO_GLOBAL);

        if (message.airshipUp_KeyDown) {
            idealLinearVelocity.add(upDirection.getProduct(.5));
        }
        if (message.airshipDown_KeyDown) {
            idealLinearVelocity.add(downDirection.getProduct(.5));
        }

        double sidePitch = 0;

        if (message.airshipRight_KeyDown) {
            idealAngularDirection.subtract(shipUp);
            sidePitch -= 10D;
        }
        if (message.airshipLeft_KeyDown) {
            idealAngularDirection.add(shipUp);
            sidePitch += 10D;
        }

        Vector sidesRotationAxis = new Vector(playerDirection);
        controlledShip.getShipTransformationManager().getCurrentTickTransform().rotate(sidesRotationAxis, TransformType.SUBSPACE_TO_GLOBAL);

        double[] rotationSidesTransform = RotationMatrices.getRotationMatrix(sidesRotationAxis.X, sidesRotationAxis.Y,
                sidesRotationAxis.Z, Math.toRadians(sidePitch));
        shipUpPosIdeal.transform(rotationSidesTransform);

        idealAngularDirection.multiply(2);
        // The vector that points in the direction of the normal of the plane that
        // contains shipUp and shipUpPos. This is our axis of rotation.
        Vector shipUpRotationVector = shipUp.cross(shipUpPosIdeal);
        // This isnt quite right, but it handles the cases quite well.
        double shipUpTheta = shipUp.angleBetween(shipUpPosIdeal) + Math.PI;
        shipUpRotationVector.multiply(shipUpTheta);

        idealAngularDirection.add(shipUpRotationVector);
        idealLinearVelocity.multiply(20D * controlledShip.getPhysicsProcessor().getMass());

        // Move the ship faster if the player holds the sprint key.
        if (message.airshipSprinting) {
            idealLinearVelocity.multiply(2D);
        }

        double lerpFactor = .2D;
        Vector linearMomentumDif = idealLinearVelocity.getSubtraction(controlledShip.getPhysicsProcessor().linearMomentum);
        Vector angularVelocityDif = idealAngularDirection.getSubtraction(controlledShip.getPhysicsProcessor().angularVelocity);

        linearMomentumDif.multiply(lerpFactor);
        angularVelocityDif.multiply(lerpFactor);

        controlledShip.getPhysicsProcessor().linearMomentum.subtract(linearMomentumDif);
        controlledShip.getPhysicsProcessor().angularVelocity.subtract(angularVelocityDif);
    }

}
