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

import java.util.ArrayList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import valkyrienwarfare.addon.control.network.HovercraftControllerGUIInputMessage;
import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.api.block.ethercompressor.TileEntityEtherCompressor;
import valkyrienwarfare.physics.calculations.PhysicsCalculations;
import valkyrienwarfare.physics.management.PhysicsObject;
import valkyrienwarfare.util.NBTUtils;

@Deprecated
public class TileEntityHoverController extends TileEntity {

    public ArrayList<BlockPos> enginePositions = new ArrayList<BlockPos>();
    public double idealHeight = 16D;
    public double stabilityBias = .45D;

    public double linearVelocityBias = 1D;
    public double angularVelocityBias = 50D;

    public Vector normalVector = new Vector(0D, 1D, 0D);

    public double angularConstant = 500000000D;
    public double linearConstant = 1000000D;

    public boolean autoStabalizerControl = false;

    public TileEntityHoverController() {
        // validate();
    }

    /*
     * Returns the Force Vector the engine will send to the physics engine
     */
    public Vector getForceForEngine(TileEntityEtherCompressor engine, World world, BlockPos enginePos, IBlockState state, PhysicsObject physObj, double secondsToApply) {
        // physObj.physicsProcessor.convertTorqueToVelocity();
        // secondsToApply*=5D;
        // idealHeight = 100D;

        Vector shipVel = new Vector(physObj.physicsProcessor.linearMomentum);

        shipVel.multiply(physObj.physicsProcessor.getInvMass());

        if (!world.isBlockPowered(getPos()) || autoStabalizerControl) {
//            setAutoStabilizationValue(physObj);
        }


        PhysicsCalculations calculations = physObj.physicsProcessor;

        double[] rotationAndTranslationMatrix = physObj.coordTransform.lToWTransform;
        Vector angularVelocity = new Vector(calculations.angularVelocity);
        Vector linearMomentum = new Vector(calculations.linearMomentum);

        double currentErrorY = -getControllerDistFromIdealY(physObj);
        double currentEngineErrorAngularY = -getEngineDistFromIdealAngular(enginePos, physObj, secondsToApply);


        Vector potentialMaxForce = new Vector(0, engine.getMaxThrust(), 0);
        potentialMaxForce.multiply(calculations.getInvMass());
        potentialMaxForce.multiply(calculations.getPhysTickSpeed());
        Vector potentialMaxThrust = engine.getPositionInLocalSpaceWithOrientation().cross(potentialMaxForce);
        RotationMatrices.applyTransform3by3(calculations.invFramedMOI, potentialMaxThrust);
        potentialMaxThrust.multiply(calculations.getPhysTickSpeed());

        double linearThama = 4.5D;
        double maxYDelta = 10D;

        double futureCurrentErrorY = currentErrorY + linearThama * potentialMaxForce.Y;
        double futureEngineErrorAngularY = getEngineDistFromIdealAngular(engine.getPos(), rotationAndTranslationMatrix, angularVelocity.getAddition(potentialMaxThrust), calculations.centerOfMass, calculations.getPhysTickSpeed());

        boolean doesForceMinimizeError = false;

        if (Math.abs(futureCurrentErrorY) < Math.abs(currentErrorY) && Math.abs(futureEngineErrorAngularY) < Math.abs(currentEngineErrorAngularY)) {
            doesForceMinimizeError = true;
            if (Math.abs(linearMomentum.Y * calculations.getInvMass()) > maxYDelta) {
                if (Math.abs((potentialMaxForce.Y + linearMomentum.Y) * calculations.getInvMass()) > Math.abs(linearMomentum.Y * calculations.getInvMass())) {
                    doesForceMinimizeError = false;
                }
            } else {
                if (Math.abs((potentialMaxForce.Y + linearMomentum.Y) * calculations.getInvMass()) > maxYDelta) {
                    doesForceMinimizeError = false;
                }
            }

        }

        if (doesForceMinimizeError) {
            engine.setThrust(engine.getMaxThrust());
            if (Math.abs(currentErrorY) < 1D) {
                engine.setThrust(engine.getMaxThrust() * Math.pow(Math.abs(currentErrorY), 3D));
            }
        } else {
            engine.setThrust(0);
        }

//		calculations.convertTorqueToVelocity();

        // System.out.println(aggregateForce);

        return engine.getForceOutputNormal().getProduct(engine.getThrust() * secondsToApply);
//		return new Vector();
    }

    public double getEngineDistFromIdealAngular(BlockPos enginePos, PhysicsObject physObj, double secondsToApply) {
        Vector controllerPos = new Vector(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);
        Vector enginePosVec = new Vector(enginePos.getX() + .5D, enginePos.getY() + .5D, enginePos.getZ() + .5D);

        controllerPos.subtract(physObj.physicsProcessor.centerOfMass);
        enginePosVec.subtract(physObj.physicsProcessor.centerOfMass);

        Vector unOrientedPosDif = new Vector(enginePosVec.X - controllerPos.X, enginePosVec.Y - controllerPos.Y, enginePosVec.Z - controllerPos.Z);

        double idealYDif = unOrientedPosDif.dot(normalVector);

        RotationMatrices.doRotationOnly(physObj.coordTransform.lToWRotation, controllerPos);
        RotationMatrices.doRotationOnly(physObj.coordTransform.lToWRotation, enginePosVec);

        double inWorldYDif = enginePosVec.Y - controllerPos.Y;

        Vector angularVelocityAtPoint = physObj.physicsProcessor.angularVelocity.cross(enginePosVec);
        angularVelocityAtPoint.multiply(secondsToApply);

        return idealYDif - (inWorldYDif + angularVelocityAtPoint.Y * angularVelocityBias);
    }

    public double getEngineDistFromIdealAngular(BlockPos enginePos, double[] lToWRotation, Vector angularVelocity, Vector centerOfMass, double secondsToApply) {

        Vector controllerPos = new Vector(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);
        Vector enginePosVec = new Vector(enginePos.getX() + .5D, enginePos.getY() + .5D, enginePos.getZ() + .5D);

        controllerPos.subtract(centerOfMass);
        enginePosVec.subtract(centerOfMass);

        Vector unOrientedPosDif = new Vector(enginePosVec.X - controllerPos.X, enginePosVec.Y - controllerPos.Y, enginePosVec.Z - controllerPos.Z);

        double idealYDif = unOrientedPosDif.dot(normalVector);

        RotationMatrices.doRotationOnly(lToWRotation, controllerPos);
        RotationMatrices.doRotationOnly(lToWRotation, enginePosVec);

        double inWorldYDif = enginePosVec.Y - controllerPos.Y;

        Vector angularVelocityAtPoint = angularVelocity.cross(enginePosVec);
        angularVelocityAtPoint.multiply(secondsToApply);

        return idealYDif - (inWorldYDif + angularVelocityAtPoint.Y * angularVelocityBias);
    }

    public double getControllerDistFromIdealY(PhysicsObject physObj) {
        Vector controllerPos = new Vector(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);
        physObj.coordTransform.fromLocalToGlobal(controllerPos);
        return idealHeight - (physObj.physicsProcessor.wrapperEnt.posY + (physObj.physicsProcessor.linearMomentum.Y * physObj.physicsProcessor.getInvMass() * linearVelocityBias * 3D));
    }

    public void handleGUIInput(HovercraftControllerGUIInputMessage message, MessageContext ctx) {
        idealHeight = message.newIdealHeight;

        if (message.newStablitiyBias < 0 || message.newStablitiyBias > 1D) {
            // Out of bounds, set to auto
//			autoStabalizerControl = true;
        } else {
            double stabilityDif = Math.abs(stabilityBias - message.newStablitiyBias);
            // if(stabilityDif>.05D){
            stabilityBias = message.newStablitiyBias;
//			autoStabalizerControl = false;
            // }
        }

        linearVelocityBias = message.newLinearVelocityBias;
        markDirty();
    }

    private void setAutoStabilizationValue(PhysicsObject physObj) {
        Vector controllerPos = new Vector(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);
        physObj.coordTransform.fromLocalToGlobal(controllerPos);

        double controllerDistToIdeal = -(idealHeight - physObj.physicsProcessor.wrapperEnt.posY);
        double yVelocity = physObj.physicsProcessor.linearMomentum.Y * physObj.physicsProcessor.getInvMass() * linearVelocityBias;

//		System.out.println("ay");

        double biasChange = .00005D;

        if (Math.abs(controllerDistToIdeal + yVelocity) > .5D) {

            if ((yVelocity > 0 && controllerDistToIdeal > 0) || (yVelocity < 0 && controllerDistToIdeal < 0)) {
                double modifiyer = 10.5;

                if (Math.abs(controllerDistToIdeal + yVelocity) < 40D) {
                    if (Math.abs(controllerDistToIdeal) > .5D) {
//						System.out.println("easy");
                        stabilityBias *= .9999D;
                    }
                } else {
//					System.out.println("hard");
                    stabilityBias -= (biasChange) * Math.max(Math.log10(Math.abs(controllerDistToIdeal + yVelocity)), 0D) * modifiyer;
                }

//				modifiyer = math.abs(controllerDistToIdeal + yVelocity)/1000D;


            } else {
//				stabilityBias += (biasChange * .25D / math.pow((math.min(.25D, math.abs(controllerDistToIdeal + yVelocity))), .5D));
            }
//			stabilityBias -= biasChange;
        } else {
            stabilityBias += (biasChange * .5D / Math.pow((Math.min(.5D, Math.abs(controllerDistToIdeal + yVelocity))), .5D)) / 10D;
        }

        stabilityBias = Math.max(Math.min(stabilityBias, 1D), 0.01D);

    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        SPacketUpdateTileEntity packet = new SPacketUpdateTileEntity(pos, 0, writeToNBT(new NBTTagCompound()));
        return packet;
    }

    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        enginePositions = NBTUtils.readBlockPosArrayListFromNBT("enginePositions", compound);
        normalVector = NBTUtils.readVectorFromNBT("normalVector", compound);
        if (normalVector.isZero()) {
            normalVector = new Vector(0, 1, 0);
        }
        idealHeight = compound.getDouble("idealHeight");
        stabilityBias = compound.getDouble("stabilityBias");
        linearVelocityBias = compound.getDouble("linearVelocityBias");
        angularVelocityBias = compound.getDouble("angularVelocityBias");
        autoStabalizerControl = compound.getBoolean("autoStabalizerControl");
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTUtils.writeBlockPosArrayListToNBT("enginePositions", enginePositions, compound);
        NBTUtils.writeVectorToNBT("normalVector", normalVector, compound);
        compound.setDouble("idealHeight", idealHeight);
        compound.setDouble("stabilityBias", stabilityBias);
        compound.setDouble("linearVelocityBias", linearVelocityBias);
        compound.setDouble("angularVelocityBias", angularVelocityBias);
        compound.setBoolean("autoStabalizerControl", autoStabalizerControl);
        return super.writeToNBT(compound);
    }

}
