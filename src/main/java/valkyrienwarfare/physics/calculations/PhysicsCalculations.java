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

package valkyrienwarfare.physics.calculations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.vecmath.Matrix3d;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.control.balloon.BalloonProcessor;
import valkyrienwarfare.addon.control.nodenetwork.IPhysicsProcessorNode;
import valkyrienwarfare.addon.control.nodenetwork.Node;
import valkyrienwarfare.api.IBlockForceProvider;
import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.math.BigBastardMath;
import valkyrienwarfare.math.Quaternion;
import valkyrienwarfare.physics.collision.ShipPhysicsCollider;
import valkyrienwarfare.physics.collision.WorldPhysicsCollider;
import valkyrienwarfare.physics.data.BlockForce;
import valkyrienwarfare.physics.data.BlockMass;
import valkyrienwarfare.physics.data.PhysicsQueuedForce;
import valkyrienwarfare.physics.management.CoordTransformObject;
import valkyrienwarfare.physics.management.PhysicsObject;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;
import valkyrienwarfare.util.NBTUtils;
import valkyrienwarfare.util.PhysicsSettings;

public class PhysicsCalculations {

    public static final double BLOCKS_TO_METERS = 1.8D;
    public static final double DRAG_CONSTANT = .99D;
    
    public final PhysicsObject parent;
    public final PhysicsWrapperEntity wrapperEnt;
    public final World worldObj;
    public final WorldPhysicsCollider worldCollision;
    public final ShipPhysicsCollider shipCollision;

    public Vector centerOfMass;
    public Vector linearMomentum;
    public Vector angularVelocity;
    public Vector torque;
    private double mass;
    public Vector gravity = new Vector(0, -9.8D, 0);
    // The time occurring on each PhysTick
    public double physRawSpeed;
    // Number of iterations the solver runs on each game tick
    public int iterations = 5;
    // The amount of time to be simulated on each rawPhysTick *(Its physSpeed/iterations)
    // Used to limit the accumulation of motion by an object (Basically Air-Resistance preventing infinite energy)
    public final List<BlockPos> activeForcePositions;
    public double[] MoITensor, invMoITensor;
    public double[] framedMOI, invFramedMOI;
    public boolean actAsArchimedes = false;
    //Used when I update the mass table, to require all old ships to recalculate their inertia matrix and mass
    @Deprecated
    public boolean isShipPastBuild91 = false;

    public PhysicsCalculations(PhysicsObject toProcess) {
        parent = toProcess;
        wrapperEnt = parent.wrapper;
        worldObj = toProcess.worldObj;
        worldCollision = new WorldPhysicsCollider(this);
        shipCollision = new ShipPhysicsCollider(this);

        MoITensor = RotationMatrices.getZeroMatrix(3);
        invMoITensor = RotationMatrices.getZeroMatrix(3);
        framedMOI = RotationMatrices.getZeroMatrix(3);
        invFramedMOI = RotationMatrices.getZeroMatrix(3);

        centerOfMass = new Vector(toProcess.centerCoord);
        linearMomentum = new Vector();
        angularVelocity = new Vector();
        torque = new Vector();
        
        activeForcePositions = new ArrayList<BlockPos>();
    }

    public PhysicsCalculations(PhysicsCalculations toCopy) {
        parent = toCopy.parent;
        wrapperEnt = toCopy.wrapperEnt;
        worldObj = toCopy.worldObj;
        worldCollision = toCopy.worldCollision;
        shipCollision = toCopy.shipCollision;
        centerOfMass = toCopy.centerOfMass;
        linearMomentum = toCopy.linearMomentum;
        angularVelocity = toCopy.angularVelocity;
        torque = toCopy.torque;
        mass = toCopy.mass;
        gravity = toCopy.gravity;
        physRawSpeed = toCopy.physRawSpeed;
        iterations = toCopy.iterations;
        activeForcePositions = toCopy.activeForcePositions;
        MoITensor = toCopy.MoITensor;
        invMoITensor = toCopy.invMoITensor;
        framedMOI = toCopy.framedMOI;
        invFramedMOI = toCopy.invFramedMOI;
        actAsArchimedes = toCopy.actAsArchimedes;
        isShipPastBuild91 = toCopy.isShipPastBuild91;
    }

    public void onSetBlockState(IBlockState oldState, IBlockState newState, BlockPos pos) {
        if (newState == oldState) {
            //Nothing changed, so don't do anything
            //Or, liquids were involved, so still don't do anything
            return;
        }

        if (oldState.getBlock() == Blocks.AIR) {
            if (BlockForce.basicForces.isBlockProvidingForce(newState, pos, worldObj)) {
                activeForcePositions.add(pos);
            }
        } else {
            // int index = activeForcePositions.indexOf(pos);
            // if(BlockForce.basicForces.isBlockProvidingForce(newState, pos, worldObj)){
            // if(index==-1){
            // activeForcePositions.add(pos);
            // }
            // }else{
            // if(index!=-1){
            // activeForcePositions.remove(index);
            // }
            // }
            if (activeForcePositions.contains(pos)) {
                if (!BlockForce.basicForces.isBlockProvidingForce(newState, pos, worldObj)) {
                    activeForcePositions.remove(pos);
                }
            } else {
                if (BlockForce.basicForces.isBlockProvidingForce(newState, pos, worldObj)) {
                    activeForcePositions.add(pos);
                }
            }
        }
        if (newState.getBlock() == Blocks.AIR) {
            activeForcePositions.remove(pos);
        }

        double oldMassAtPos = BlockMass.basicMass.getMassFromState(oldState, pos, worldObj);
        double newMassAtPos = BlockMass.basicMass.getMassFromState(newState, pos, worldObj);
        // Don't change anything if the mass is the same
        if (oldMassAtPos != newMassAtPos) {
            double notAHalf = .4D;
            double x = pos.getX() + .5D;
            double y = pos.getY() + .5D;
            double z = pos.getZ() + .5D;

            if (oldMassAtPos > 0D) {
                oldMassAtPos /= -9.0D;
                addMassAt(x, y, z, oldMassAtPos);
                addMassAt(x + notAHalf, y + notAHalf, z + notAHalf, oldMassAtPos);
                addMassAt(x + notAHalf, y + notAHalf, z - notAHalf, oldMassAtPos);
                addMassAt(x + notAHalf, y - notAHalf, z + notAHalf, oldMassAtPos);
                addMassAt(x + notAHalf, y - notAHalf, z - notAHalf, oldMassAtPos);
                addMassAt(x - notAHalf, y + notAHalf, z + notAHalf, oldMassAtPos);
                addMassAt(x - notAHalf, y + notAHalf, z - notAHalf, oldMassAtPos);
                addMassAt(x - notAHalf, y - notAHalf, z + notAHalf, oldMassAtPos);
                addMassAt(x - notAHalf, y - notAHalf, z - notAHalf, oldMassAtPos);
            }
            if (newMassAtPos > 0D) {
                newMassAtPos /= 9.0D;
                addMassAt(x, y, z, newMassAtPos);
                addMassAt(x + notAHalf, y + notAHalf, z + notAHalf, newMassAtPos);
                addMassAt(x + notAHalf, y + notAHalf, z - notAHalf, newMassAtPos);
                addMassAt(x + notAHalf, y - notAHalf, z + notAHalf, newMassAtPos);
                addMassAt(x + notAHalf, y - notAHalf, z - notAHalf, newMassAtPos);
                addMassAt(x - notAHalf, y + notAHalf, z + notAHalf, newMassAtPos);
                addMassAt(x - notAHalf, y + notAHalf, z - notAHalf, newMassAtPos);
                addMassAt(x - notAHalf, y - notAHalf, z + notAHalf, newMassAtPos);
                addMassAt(x - notAHalf, y - notAHalf, z - notAHalf, newMassAtPos);
            }
        }
    }

    private void addMassAt(double x, double y, double z, double addedMass) {
        Vector prevCenterOfMass = new Vector(centerOfMass);
        if (mass > .0001D) {
            centerOfMass.multiply(mass);
            centerOfMass.add(new Vector(x, y, z).getProduct(addedMass));
            centerOfMass.multiply(1.0D / (mass + addedMass));
        } else {
            centerOfMass = new Vector(x, y, z);
            MoITensor = RotationMatrices.getZeroMatrix(3);
        }
        double cmShiftX = prevCenterOfMass.X - centerOfMass.X;
        double cmShiftY = prevCenterOfMass.Y - centerOfMass.Y;
        double cmShiftZ = prevCenterOfMass.Z - centerOfMass.Z;
        double rx = x - centerOfMass.X;
        double ry = y - centerOfMass.Y;
        double rz = z - centerOfMass.Z;

        MoITensor[0] = MoITensor[0] + (cmShiftY * cmShiftY + cmShiftZ * cmShiftZ) * mass + (ry * ry + rz * rz) * addedMass;
        MoITensor[1] = MoITensor[1] - cmShiftX * cmShiftY * mass - rx * ry * addedMass;
        MoITensor[2] = MoITensor[2] - cmShiftX * cmShiftZ * mass - rx * rz * addedMass;
        MoITensor[3] = MoITensor[1];
        MoITensor[4] = MoITensor[4] + (cmShiftX * cmShiftX + cmShiftZ * cmShiftZ) * mass + (rx * rx + rz * rz) * addedMass;
        MoITensor[5] = MoITensor[5] - cmShiftY * cmShiftZ * mass - ry * rz * addedMass;
        MoITensor[6] = MoITensor[2];
        MoITensor[7] = MoITensor[5];
        MoITensor[8] = MoITensor[8] + (cmShiftX * cmShiftX + cmShiftY * cmShiftY) * mass + (rx * rx + ry * ry) * addedMass;

        mass += addedMass;
        invMoITensor = RotationMatrices.inverse3by3(MoITensor);
        // angularVelocity = RotationMatrices.get3by3TransformedVec(oldMOI, torque);
        // angularVelocity = RotationMatrices.get3by3TransformedVec(invMoITensor, torque);
        // System.out.println(MoITensor[0]+":"+MoITensor[1]+":"+MoITensor[2]);
        // System.out.println(MoITensor[3]+":"+MoITensor[4]+":"+MoITensor[5]);
        // System.out.println(MoITensor[6]+":"+MoITensor[7]+":"+MoITensor[8]);
    }

    public void rawPhysTickPreCol(double newPhysSpeed, int iters) {
        if (!isShipPastBuild91) {
            recalculateInertiaMatrices();
            isShipPastBuild91 = true;
        }
        if (parent.doPhysics) {
            updatePhysSpeedAndIters(newPhysSpeed, iters);
            updateParentCenterOfMass();
            calculateFramedMOITensor();
            if (!actAsArchimedes) {
                sendPhysicsProcessorsTicks();
                calculateForces();
            } else {
                calculateForcesArchimedes();
            }
        }
    }

    public void processWorldCollision() {
        if (parent.doPhysics) {
            worldCollision.runPhysCollision();
        }
    }

    public void rawPhysTickPostCol() {
        if (parent.doPhysics) {
            if (!arePhysicsGoingWayTooFast()) {
                if (PhysicsSettings.doAirshipRotation) {
                    applyAngularVelocity();
                }
                if (PhysicsSettings.doAirshipMovement) {
                    applyLinearVelocity();
                }
            } else {
                parent.doPhysics = false;
                linearMomentum.zero();
                angularVelocity.zero();
            }
        }
    }

    private boolean arePhysicsGoingWayTooFast() {
        if (angularVelocity.lengthSq() > 50000) {
            System.out.println("Ship tried moving too fast; freezing it and reseting velocities");
            return true;
        }

        //This says if ship is moving faster than 10 blocks per second
        if (linearMomentum.lengthSq() * getInvMass() * getInvMass() > 50000) {
            System.out.println("Ship tried moving too fast; freezing it and reseting velocities");
            return true;
        }
        return false;
    }

    // The x/y/z variables need to be updated when the centerOfMass location changes
    public void updateParentCenterOfMass() {
        Vector parentCM = parent.centerCoord;
        if (!parent.centerCoord.equals(centerOfMass)) {
            Vector CMDif = centerOfMass.getSubtraction(parentCM);
            RotationMatrices.applyTransform(parent.coordTransform.lToWRotation, CMDif);

            parent.wrapper.posX -= CMDif.X;
            parent.wrapper.posY -= CMDif.Y;
            parent.wrapper.posZ -= CMDif.Z;

            parent.centerCoord = new Vector(centerOfMass);
            parent.coordTransform.updateAllTransforms();
        }
    }

    // Applies the rotation transform onto the Moment of Inertia to generate the REAL MOI at that given instant
    private void calculateFramedMOITensor() {
        framedMOI = new double[9];
        Matrix3d pitch = new Matrix3d();
        Matrix3d yaw = new Matrix3d();
        Matrix3d roll = new Matrix3d();
        pitch.rotX(Math.toRadians(parent.wrapper.pitch));
        yaw.rotY(Math.toRadians(parent.wrapper.yaw));
        roll.rotZ(Math.toRadians(parent.wrapper.roll));
        pitch.mul(yaw);
        pitch.mul(roll);
        pitch.normalize();
        Matrix3d inertiaBodyFrame = new Matrix3d(MoITensor);
        Matrix3d multipled = new Matrix3d();
        multipled.mul(pitch, inertiaBodyFrame);
        pitch.transpose();
        multipled.mul(pitch);
        framedMOI[0] = multipled.m00;
        framedMOI[1] = multipled.m01;
        framedMOI[2] = multipled.m02;
        framedMOI[3] = multipled.m10;
        framedMOI[4] = multipled.m11;
        framedMOI[5] = multipled.m12;
        framedMOI[6] = multipled.m20;
        framedMOI[7] = multipled.m21;
        framedMOI[8] = multipled.m22;
        invFramedMOI = RotationMatrices.inverse3by3(framedMOI);
    }

    protected void calculateForces() {
        double modifiedDrag = Math.pow(DRAG_CONSTANT, getPhysTickSpeed() / .05D);
        linearMomentum.multiply(modifiedDrag);
        angularVelocity.multiply(modifiedDrag);
        applyGravity();
        addQueuedForces();
        Collections.shuffle(activeForcePositions);

        Vector blockForce = new Vector();
        Vector inBodyWO = new Vector();
        Vector crossVector = new Vector();

        if (PhysicsSettings.doPhysicsBlocks) {
            for (Node node : parent.nodesWithinShip) {
                TileEntity nodeTile = node.getParentTile();
                if (nodeTile instanceof IPhysicsProcessorNode) {
//					System.out.println("test");
                    ((IPhysicsProcessorNode) nodeTile).onPhysicsTick(parent, this, physRawSpeed);
                }
            }

            for (BlockPos pos : activeForcePositions) {
                IBlockState state = parent.VKChunkCache.getBlockState(pos);
                Block blockAt = state.getBlock();
                BigBastardMath.getBodyPosWithOrientation(pos, centerOfMass, parent.coordTransform.lToWRotation, inBodyWO);

                BlockForce.basicForces.getForceFromState(state, pos, worldObj, getPhysTickSpeed(), parent, blockForce);

                if (blockForce != null) {
                    if (blockAt instanceof IBlockForceProvider) {
                        Vector otherPosition = ((IBlockForceProvider) blockAt).getCustomBlockForcePosition(worldObj, pos, state, parent.wrapper, getPhysTickSpeed());
                        if (otherPosition != null) {
                            BigBastardMath.getBodyPosWithOrientation(otherPosition, centerOfMass, parent.coordTransform.lToWRotation, inBodyWO);
                        }
                    }
                    addForceAtPoint(inBodyWO, blockForce, crossVector);
                }
            }
        }

        if (PhysicsSettings.doBalloons) {
            for (BalloonProcessor balloon : parent.balloonManager.balloonProcessors) {
                balloon.tickBalloonTemperatures(getPhysTickSpeed(), this);

                Vector balloonForce = balloon.getBalloonForce(getPhysTickSpeed(), this);
                Vector balloonCenterInBody = balloon.getForceCenter();

                BigBastardMath.getBodyPosWithOrientation(balloonCenterInBody, centerOfMass, parent.coordTransform.lToWRotation, inBodyWO);

                addForceAtPoint(inBodyWO, balloonForce, crossVector);
            }
        }

        convertTorqueToVelocity();
    }

    public void applyGravity() {
        if (PhysicsSettings.doGravity) {
            addForceAtPoint(new Vector(0, 0, 0), ValkyrienWarfareMod.gravity.getProduct(mass * getPhysTickSpeed()));
        }
    }

    public void calculateForcesArchimedes() {
        double modifiedDrag = Math.pow(DRAG_CONSTANT, getPhysTickSpeed() / .05D);
        linearMomentum.multiply(modifiedDrag);
        angularVelocity.multiply(modifiedDrag);
    }

    public void sendPhysicsProcessorsTicks() {
//		HashSet<nodenetwork> shipNodeNetworks = parent.nodeNetworksWithinShip;
//		for(nodenetwork network : shipNodeNetworks){
//			System.out.println("Dayum");
//		}
    }

    public void addQueuedForces() {
        Collections.shuffle(parent.queuedPhysForces);
        for (PhysicsQueuedForce queuedForce : parent.queuedPhysForces) {
            Vector forceVec = new Vector(queuedForce.force);
            if (queuedForce.isLocal) {
                RotationMatrices.doRotationOnly(parent.coordTransform.lToWRotation, forceVec);
            }
            forceVec.multiply(getPhysTickSpeed());

            Vector posVec = new Vector(queuedForce.inBodyPos);
            // RotationMatrices.applyTransform(parent.coordTransform.lToWTransform, posVec);
            posVec.X -= wrapperEnt.posX;
            posVec.Y -= wrapperEnt.posY;
            posVec.Z -= wrapperEnt.posZ;

            addForceAtPoint(posVec, forceVec);
            // System.out.println(posVec);
            // torque.add(posVec.cross(forceVec));
            // linearMomentum.add(forceVec);
        }
    }

    public void convertTorqueToVelocity() {
        if (!torque.isZero()) {
            angularVelocity.add(RotationMatrices.get3by3TransformedVec(invFramedMOI, torque));
            torque.zero();
        }
    }

    public void addForceAtPoint(Vector inBodyWO, Vector forceToApply) {
        forceToApply.multiply(BLOCKS_TO_METERS);
        torque.add(inBodyWO.cross(forceToApply));
        linearMomentum.add(forceToApply);
    }

    public void addForceAtPoint(Vector inBodyWO, Vector forceToApply, Vector crossVector) {
        forceToApply.multiply(BLOCKS_TO_METERS);
        crossVector.setCross(inBodyWO, forceToApply);
        torque.add(crossVector);
        linearMomentum.add(forceToApply);
    }

    public void updatePhysSpeedAndIters(double newPhysSpeed, int iters) {
        physRawSpeed = newPhysSpeed;
        iterations = iters;
    }

    public void applyAngularVelocity() {
        CoordTransformObject coordTrans = parent.coordTransform;

        double[] rotationChange = RotationMatrices.getRotationMatrix(angularVelocity.X, angularVelocity.Y, angularVelocity.Z, angularVelocity.length() * getPhysTickSpeed());
        Quaternion finalTransform = Quaternion.QuaternionFromMatrix(RotationMatrices.getMatrixProduct(rotationChange, coordTrans.lToWRotation));

        double[] radians = finalTransform.toRadians();
        wrapperEnt.pitch = Double.isNaN(radians[0]) ? 0.0f : (float) Math.toDegrees(radians[0]);
        wrapperEnt.yaw = Double.isNaN(radians[1]) ? 0.0f : (float) Math.toDegrees(radians[1]);
        wrapperEnt.roll = Double.isNaN(radians[2]) ? 0.0f : (float) Math.toDegrees(radians[2]);
        coordTrans.updateAllTransforms();
    }

    public void applyLinearVelocity() {
        if (mass > 0) {
            double momentMod = getPhysTickSpeed() * getInvMass();
            wrapperEnt.posX += (linearMomentum.X * momentMod);
            wrapperEnt.posY += (linearMomentum.Y * momentMod);
            wrapperEnt.posZ += (linearMomentum.Z * momentMod);
        }
        if (wrapperEnt.posY > ValkyrienWarfareMod.shipUpperLimit) {
            wrapperEnt.posY = ValkyrienWarfareMod.shipUpperLimit;
        }
        if (wrapperEnt.posY < ValkyrienWarfareMod.shipLowerLimit) {
            wrapperEnt.posY = ValkyrienWarfareMod.shipLowerLimit;
        }
    }

    public Vector getVelocityAtPoint(Vector inBodyWO) {
        Vector speed = angularVelocity.cross(inBodyWO);
        double invMass = getInvMass();
        speed.X += (linearMomentum.X * invMass);
        speed.Y += (linearMomentum.Y * invMass);
        speed.Z += (linearMomentum.Z * invMass);
        return speed;
    }

    public void setVectorToVelocityAtPoint(Vector inBodyWO, Vector toSet) {
        toSet.setCross(angularVelocity, inBodyWO);
        double invMass = getInvMass();
        toSet.X += (linearMomentum.X * invMass);
        toSet.Y += (linearMomentum.Y * invMass);
        toSet.Z += (linearMomentum.Z * invMass);
    }

    public void writeToNBTTag(NBTTagCompound compound) {
        compound.setDouble("mass", mass);

        NBTUtils.writeVectorToNBT("linear", linearMomentum, compound);
        NBTUtils.writeVectorToNBT("angularVelocity", angularVelocity, compound);
        NBTUtils.writeVectorToNBT("CM", centerOfMass, compound);

        NBTUtils.write3x3MatrixToNBT("MOI", MoITensor, compound);
        compound.setBoolean("isShipPastBuild91", isShipPastBuild91);
    }

    public void readFromNBTTag(NBTTagCompound compound) {
        mass = compound.getDouble("mass");

        linearMomentum = NBTUtils.readVectorFromNBT("linear", compound);
        angularVelocity = NBTUtils.readVectorFromNBT("angularVelocity", compound);
        centerOfMass = NBTUtils.readVectorFromNBT("CM", compound);

        MoITensor = NBTUtils.read3x3MatrixFromNBT("MOI", compound);

        isShipPastBuild91 = compound.getBoolean("isShipPastBuild91");
        processNBTRead();
    }

    // Calculates the inverses and the framed MOIs
    public void processNBTRead() {
        invMoITensor = RotationMatrices.inverse3by3(MoITensor);
    }

    // Called upon a Ship being created from the World, and generates the physics data for it
    public void processInitialPhysicsData() {
        IBlockState Air = Blocks.AIR.getDefaultState();
        for (BlockPos pos : parent.blockPositions) {
            onSetBlockState(Air, parent.VKChunkCache.getBlockState(pos), pos);
        }
    }

    @Deprecated
    //Temp code that upgrades old ships to new BlockMass System; Will remove in future
    private void recalculateInertiaMatrices() {
        mass = 0;
        centerOfMass = new Vector(parent.refrenceBlockPos.getX() + .5D, parent.refrenceBlockPos.getY() + .5D, parent.refrenceBlockPos.getZ() + .5D);
        IBlockState airState = Blocks.AIR.getDefaultState();
        activeForcePositions.clear();
        for (BlockPos pos : parent.blockPositions) {
            onSetBlockState(airState, parent.VKChunkCache.getBlockState(pos), pos);
        }
        System.out.println("Recalculated physics inertia matrix for an old Ship of size " + parent.blockPositions.size());
        processNBTRead();
    }
    
    // These getter methods guarantee that only code within this class can modify the mass,
    // preventing outside code from breaking things
    public double getMass() {
        return mass;
    }
    
    public double getInvMass() {
        return 1D / mass;
    }
    
    public double getPhysTickSpeed() {
        return physRawSpeed / iterations;
    }

}
