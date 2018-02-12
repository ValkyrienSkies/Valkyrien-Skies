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
import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physics.management.PhysicsObject;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

public class TileEntityPilotsChair extends ImplTileEntityPilotable {

    @Override
    void processControlMessage(PilotControlsMessage message, EntityPlayerMP sender) {
        IBlockState blockState = getWorld().getBlockState(getPos());
        if (blockState.getBlock() == ValkyrienWarfareControl.INSTANCE.blocks.pilotsChair) {
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
    final boolean setClientPilotingEntireShip() {
        return true;
    }


    @Override
    public final void onStartTileUsage(EntityPlayer player) {
        getParentPhysicsEntity().wrapping.physicsProcessor.actAsArchimedes = true;
    }

    @Override
    public final void onStopTileUsage() {
        getParentPhysicsEntity().wrapping.physicsProcessor.actAsArchimedes = false;
    }

    private final void processCalculationsForControlMessageAndApplyCalculations(PhysicsWrapperEntity wrapper, PilotControlsMessage message, IBlockState state) {
        BlockPos chairPosition = getPos();
        PhysicsObject controlledShip = wrapper.wrapping;

        double pilotPitch = 0D;
        double pilotYaw = ((BlockShipPilotsChair) state.getBlock()).getChairYaw(state, chairPosition);
        double pilotRoll = 0D;

        double[] pilotRotationMatrix = RotationMatrices.getRotationMatrix(pilotPitch, pilotYaw, pilotRoll);

        Vector playerDirection = new Vector(1, 0, 0);

        Vector rightDirection = new Vector(0, 0, 1);

        Vector leftDirection = new Vector(0, 0, -1);

        RotationMatrices.applyTransform(pilotRotationMatrix, playerDirection);
        RotationMatrices.applyTransform(pilotRotationMatrix, rightDirection);
        RotationMatrices.applyTransform(pilotRotationMatrix, leftDirection);

        Vector upDirection = new Vector(0, 1, 0);

        Vector downDirection = new Vector(0, -1, 0);

        Vector idealAngularDirection = new Vector();

        Vector idealLinearVelocity = new Vector();

        Vector shipUp = new Vector(0, 1, 0);
        Vector shipUpPos = new Vector(0, 1, 0);

        if (message.airshipForward_KeyDown) {
            idealLinearVelocity.add(playerDirection);
        }
        if (message.airshipBackward_KeyDown) {
            idealLinearVelocity.subtract(playerDirection);
        }

        RotationMatrices.applyTransform(controlledShip.coordTransform.lToWRotation, idealLinearVelocity);

        RotationMatrices.applyTransform(controlledShip.coordTransform.lToWRotation, shipUp);

        if (message.airshipUp_KeyDown) {
            idealLinearVelocity.add(upDirection);
        }
        if (message.airshipDown_KeyDown) {
            idealLinearVelocity.add(downDirection);
        }


        if (message.airshipRight_KeyDown) {
            idealAngularDirection.add(rightDirection);
        }
        if (message.airshipLeft_KeyDown) {
            idealAngularDirection.add(leftDirection);
        }

        //Upside down if you want it
//		Vector shipUpOffset = shipUpPos.getSubtraction(shipUp);
        Vector shipUpOffset = shipUp.getSubtraction(shipUpPos);

        double mass = controlledShip.physicsProcessor.getMass();

//		idealAngularDirection.multiply(mass/2.5D);
        idealLinearVelocity.multiply(mass / 5D);
//		shipUpOffset.multiply(mass/2.5D);


        idealAngularDirection.multiply(1D / 6D);
        shipUpOffset.multiply(1D / 3D);

        Vector velocityCompenstationLinear = controlledShip.physicsProcessor.linearMomentum;

        Vector velocityCompensationAngular = controlledShip.physicsProcessor.angularVelocity.cross(playerDirection);

        Vector velocityCompensationAlignment = controlledShip.physicsProcessor.angularVelocity.cross(shipUpPos);

        velocityCompensationAlignment.multiply(controlledShip.physicsProcessor.physRawSpeed);
        velocityCompensationAngular.multiply(2D * controlledShip.physicsProcessor.physRawSpeed);

        shipUpOffset.subtract(velocityCompensationAlignment);
        velocityCompensationAngular.subtract(velocityCompensationAngular);

        RotationMatrices.applyTransform3by3(controlledShip.physicsProcessor.framedMOI, idealAngularDirection);
        RotationMatrices.applyTransform3by3(controlledShip.physicsProcessor.framedMOI, shipUpOffset);


        if (message.airshipSprinting) {
            idealLinearVelocity.multiply(2D);
        }

        idealLinearVelocity.subtract(idealAngularDirection);
        idealLinearVelocity.subtract(shipUpOffset);

        //TEMPORARY CODE!!!

        controlledShip.physicsProcessor.addForceAtPoint(playerDirection, idealAngularDirection);

        controlledShip.physicsProcessor.addForceAtPoint(shipUpPos, shipUpOffset);

        controlledShip.physicsProcessor.addForceAtPoint(new Vector(), idealLinearVelocity);

        controlledShip.physicsProcessor.convertTorqueToVelocity();
    }

}
