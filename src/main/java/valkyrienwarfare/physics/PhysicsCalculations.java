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

package valkyrienwarfare.physics;

import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.vecmath.Matrix3d;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.control.nodenetwork.INodeController;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.deprecated_api.IBlockForceProvider;
import valkyrienwarfare.deprecated_api.IBlockTorqueProvider;
import valkyrienwarfare.math.Quaternion;
import valkyrienwarfare.math.RotationMatrices;
import valkyrienwarfare.math.VWMath;
import valkyrienwarfare.math.Vector;
import valkyrienwarfare.mod.coordinates.ShipTransform;
import valkyrienwarfare.mod.multithreaded.PhysicsShipTransform;
import valkyrienwarfare.physics.collision.WorldPhysicsCollider;
import valkyrienwarfare.physics.management.PhysicsObject;
import valkyrienwarfare.physics.management.ShipTransformationManager;
import valkyrienwarfare.util.NBTUtils;
import valkyrienwarfare.util.PhysicsSettings;

public class PhysicsCalculations {

    // TODO: Kill this constant
	@Deprecated
    public static final double PHYSICS_SPEEDUP_FACTOR = 1.0D;
    public static final double DRAG_CONSTANT = .99D;
    public static final double INERTIA_OFFSET = .4D;
    public static final double EPSILON = .00000001;

    private final PhysicsObject parent;
    private final WorldPhysicsCollider worldCollision;
    private final PhysicsParticleManager particleManager;
    // CopyOnWrite to provide concurrency between threads.
    private final Set<BlockPos> activeForcePositions;
    public Vector gameTickCenterOfMass;
    public Vector linearMomentum;
    public Vector angularVelocity;
    public boolean actAsArchimedes = false;
    private Vector physCenterOfMass;
    private Vector torque;
    private double gameTickMass;
    // TODO: Get this in one day
    // private double physMass;
    // The time occurring on each PhysTick
    private double physTickTimeDelta;
    private double[] gameMoITensor;
    private double[] physMOITensor;
    private double[] physInvMOITensor;
    private double physRoll, physPitch, physYaw;
    private double physX, physY, physZ;

    public PhysicsCalculations(PhysicsObject toProcess) {
        parent = toProcess;
        worldCollision = new WorldPhysicsCollider(this);
        particleManager = new PhysicsParticleManager(this);

        gameMoITensor = RotationMatrices.getZeroMatrix(3);
        physMOITensor = RotationMatrices.getZeroMatrix(3);
        setPhysInvMOITensor(RotationMatrices.getZeroMatrix(3));

        gameTickCenterOfMass = new Vector(toProcess.getCenterCoord());
        linearMomentum = new Vector();
        physCenterOfMass = new Vector();
        angularVelocity = new Vector();
        torque = new Vector();
        // We need thread safe access to this.
        activeForcePositions = ConcurrentHashMap.newKeySet();
    }

    public void onSetBlockState(IBlockState oldState, IBlockState newState, BlockPos pos) {
        World worldObj = getParent().getWorldObj();
        if (!newState.equals(oldState)) {
            if (oldState.getBlock() == Blocks.AIR) {
                if (BlockForce.basicForces.isBlockProvidingForce(newState, pos, worldObj)) {
                    activeForcePositions.add(pos);
                }
            } else {
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

            double oldMass = BlockMass.basicMass.getMassFromState(oldState, pos, worldObj);
            double newMass = BlockMass.basicMass.getMassFromState(newState, pos, worldObj);
            double deltaMass = newMass - oldMass;
            // Don't change anything if the mass is the same
            if (Math.abs(deltaMass) > EPSILON) {
                double x = pos.getX() + .5D;
                double y = pos.getY() + .5D;
                double z = pos.getZ() + .5D;

                deltaMass /= 9D;
                addMassAt(x, y, z, deltaMass);
                addMassAt(x + INERTIA_OFFSET, y + INERTIA_OFFSET, z + INERTIA_OFFSET, deltaMass);
                addMassAt(x + INERTIA_OFFSET, y + INERTIA_OFFSET, z - INERTIA_OFFSET, deltaMass);
                addMassAt(x + INERTIA_OFFSET, y - INERTIA_OFFSET, z + INERTIA_OFFSET, deltaMass);
                addMassAt(x + INERTIA_OFFSET, y - INERTIA_OFFSET, z - INERTIA_OFFSET, deltaMass);
                addMassAt(x - INERTIA_OFFSET, y + INERTIA_OFFSET, z + INERTIA_OFFSET, deltaMass);
                addMassAt(x - INERTIA_OFFSET, y + INERTIA_OFFSET, z - INERTIA_OFFSET, deltaMass);
                addMassAt(x - INERTIA_OFFSET, y - INERTIA_OFFSET, z + INERTIA_OFFSET, deltaMass);
                addMassAt(x - INERTIA_OFFSET, y - INERTIA_OFFSET, z - INERTIA_OFFSET, deltaMass);
            }
        }
    }

    private void addMassAt(double x, double y, double z, double addedMass) {
        Vector prevCenterOfMass = new Vector(gameTickCenterOfMass);
        if (gameTickMass > .0001D) {
            gameTickCenterOfMass.multiply(gameTickMass);
            gameTickCenterOfMass.add(new Vector(x, y, z).getProduct(addedMass));
            gameTickCenterOfMass.multiply(1.0D / (gameTickMass + addedMass));
        } else {
            gameTickCenterOfMass = new Vector(x, y, z);
            gameMoITensor = RotationMatrices.getZeroMatrix(3);
        }
        double cmShiftX = prevCenterOfMass.X - gameTickCenterOfMass.X;
        double cmShiftY = prevCenterOfMass.Y - gameTickCenterOfMass.Y;
        double cmShiftZ = prevCenterOfMass.Z - gameTickCenterOfMass.Z;
        double rx = x - gameTickCenterOfMass.X;
        double ry = y - gameTickCenterOfMass.Y;
        double rz = z - gameTickCenterOfMass.Z;

        gameMoITensor[0] = gameMoITensor[0] + (cmShiftY * cmShiftY + cmShiftZ * cmShiftZ) * gameTickMass
                + (ry * ry + rz * rz) * addedMass;
        gameMoITensor[1] = gameMoITensor[1] - cmShiftX * cmShiftY * gameTickMass - rx * ry * addedMass;
        gameMoITensor[2] = gameMoITensor[2] - cmShiftX * cmShiftZ * gameTickMass - rx * rz * addedMass;
        gameMoITensor[3] = gameMoITensor[1];
        gameMoITensor[4] = gameMoITensor[4] + (cmShiftX * cmShiftX + cmShiftZ * cmShiftZ) * gameTickMass
                + (rx * rx + rz * rz) * addedMass;
        gameMoITensor[5] = gameMoITensor[5] - cmShiftY * cmShiftZ * gameTickMass - ry * rz * addedMass;
        gameMoITensor[6] = gameMoITensor[2];
        gameMoITensor[7] = gameMoITensor[5];
        gameMoITensor[8] = gameMoITensor[8] + (cmShiftX * cmShiftX + cmShiftY * cmShiftY) * gameTickMass
                + (rx * rx + ry * ry) * addedMass;

        // Do this to avoid a mass of zero, which runs the risk of dividing by zero and
        // crashing the program.
        if (gameTickMass + addedMass < .0001D) {
            gameTickMass = .0001D;
            getParent().setPhysicsEnabled(false);
        } else {
            gameTickMass += addedMass;
        }
    }

    public void rawPhysTickPreCol(double newPhysSpeed) {
        if (getParent().getShipTransformationManager().getCurrentPhysicsTransform() == ShipTransformationManager.ZERO_TRANSFORM) {
            // Create a new physics transform.
            physRoll = getParent().getWrapperEntity().getRoll();
            physPitch = getParent().getWrapperEntity().getPitch();
            physYaw = getParent().getWrapperEntity().getYaw();
            physX = getParent().getWrapperEntity().posX;
            physY = getParent().getWrapperEntity().posY;
            physZ = getParent().getWrapperEntity().posZ;
            physCenterOfMass.setValue(gameTickCenterOfMass);
            ShipTransform physicsTransform = new PhysicsShipTransform(physX, physY, physZ, physPitch, physYaw, physRoll,
                    physCenterOfMass, getParent().getShipBoundingBox(),
                    getParent().getShipTransformationManager().getCurrentTickTransform());
            getParent().getShipTransformationManager().setCurrentPhysicsTransform(physicsTransform);
            getParent().getShipTransformationManager().updatePreviousPhysicsTransform();
        }
        if (getParent().isPhysicsEnabled()) {
            updatePhysSpeedAndIters(newPhysSpeed);
            updateParentCenterOfMass();
            calculateFramedMOITensor();
            if (!actAsArchimedes) {
                calculateForces();
            } else {
                calculateForcesArchimedes();
            }
        }
    }

	public void rawPhysTickPostCol() {
		if (!isPhysicsBroken()) {
			if (getParent().isPhysicsEnabled()) {
				// This wasn't implemented very well at all! Maybe in the future I'll try again.
				// enforceStaticFriction();
				if (PhysicsSettings.doAirshipRotation) {
					applyAngularVelocity();
				}
				if (PhysicsSettings.doAirshipMovement) {
					applyLinearVelocity();
				}
			}
		} else {
			getParent().setPhysicsEnabled(false);
			linearMomentum.zero();
			angularVelocity.zero();
		}

		PhysicsShipTransform finalPhysTransform = new PhysicsShipTransform(physX, physY, physZ, physPitch, physYaw,
				physRoll, physCenterOfMass, getParent().getShipBoundingBox(),
				getParent().getShipTransformationManager().getCurrentTickTransform());

		getParent().getShipTransformationManager().updatePreviousPhysicsTransform();
		getParent().getShipTransformationManager().setCurrentPhysicsTransform(finalPhysTransform);

		updatePhysCenterOfMass();
		// Moved out to VW Thread. Code run in this class should have no direct effect
		// on the physics object.
		// parent.coordTransform.updateAllTransforms(true, true);
	}

    // If the ship is moving at these speeds, its likely something in the physics
    // broke. This method helps detect that.
    private boolean isPhysicsBroken() {
        if (angularVelocity.lengthSq() > 50000 || linearMomentum.lengthSq() * getInvMass() * getInvMass() > 50000) {
            System.out.println("Ship tried moving too fast; freezing it and reseting velocities");
            return true;
        }
        return false;
    }
    
    /**
     * This method will set the linear and angular velocities to zero if both are too small.
     */
    private void enforceStaticFriction() {
        if (angularVelocity.lengthSq() < .001) {
        	double linearSpeedSq = linearMomentum.lengthSq() * this.getInvMass() * this.getInvMass();
        	if (linearSpeedSq < .05) {
        		angularVelocity.zero();
        		if (linearSpeedSq < .0001) {
        			linearMomentum.zero();
        		}
        	}
        }
    }

    // The x/y/z variables need to be updated when the centerOfMass location
    // changes.
    public void updateParentCenterOfMass() {
        Vector parentCM = getParent().getCenterCoord();
        if (!getParent().getCenterCoord().equals(gameTickCenterOfMass)) {
            Vector CMDif = gameTickCenterOfMass.getSubtraction(parentCM);
            // RotationMatrices.doRotationOnly(parent.coordTransform.lToWTransform, CMDif);

            getParent().getShipTransformationManager().getCurrentPhysicsTransform().rotate(CMDif, TransformType.SUBSPACE_TO_GLOBAL);
            getParent().getWrapperEntity().posX -= CMDif.X;
            getParent().getWrapperEntity().posY -= CMDif.Y;
            getParent().getWrapperEntity().posZ -= CMDif.Z;

            getParent().getCenterCoord().setValue(gameTickCenterOfMass);
            // parent.coordTransform.updateAllTransforms(false, false);
        }
    }

    /**
     * Updates the physics center of mass to the game center of mass; does not do
     * any transformation updates on its own.
     */
    private void updatePhysCenterOfMass() {
        if (!physCenterOfMass.equals(gameTickCenterOfMass)) {
            Vector CMDif = physCenterOfMass.getSubtraction(gameTickCenterOfMass);
            // RotationMatrices.doRotationOnly(parent.coordTransform.lToWTransform, CMDif);

            getParent().getShipTransformationManager().getCurrentPhysicsTransform().rotate(CMDif, TransformType.SUBSPACE_TO_GLOBAL);
            physX += CMDif.X;
            physY += CMDif.Y;
            physZ += CMDif.Z;

            physCenterOfMass.setValue(gameTickCenterOfMass);
        }
    }

    /**
     * Generates the rotated moment of inertia tensor with the body; uses the
     * following formula: I' = R * I * R-transpose; where I' is the rotated inertia,
     * I is unrotated interim, and R is the rotation matrix.
     */
    private void calculateFramedMOITensor() {
        double[] framedMOI = RotationMatrices.getZeroMatrix(3);

        double[] internalRotationMatrix = getParent().getShipTransformationManager().getCurrentPhysicsTransform()
                .getInternalMatrix(TransformType.SUBSPACE_TO_GLOBAL);

        // Copy the rotation matrix, ignore the translation and scaling parts.
        Matrix3d rotationMatrix = new Matrix3d(internalRotationMatrix[0], internalRotationMatrix[1],
                internalRotationMatrix[2], internalRotationMatrix[4], internalRotationMatrix[5],
                internalRotationMatrix[6], internalRotationMatrix[8], internalRotationMatrix[9],
                internalRotationMatrix[10]);

        Matrix3d inertiaBodyFrame = new Matrix3d(gameMoITensor);
        // The product of the overall rotation matrix with the inertia tensor.
        inertiaBodyFrame.mul(rotationMatrix);
        rotationMatrix.transpose();
        // The product of the inertia tensor multiplied with the transpose of the
        // rotation transpose.
        inertiaBodyFrame.mul(rotationMatrix);
        framedMOI[0] = inertiaBodyFrame.m00;
        framedMOI[1] = inertiaBodyFrame.m01;
        framedMOI[2] = inertiaBodyFrame.m02;
        framedMOI[3] = inertiaBodyFrame.m10;
        framedMOI[4] = inertiaBodyFrame.m11;
        framedMOI[5] = inertiaBodyFrame.m12;
        framedMOI[6] = inertiaBodyFrame.m20;
        framedMOI[7] = inertiaBodyFrame.m21;
        framedMOI[8] = inertiaBodyFrame.m22;

        physMOITensor = framedMOI;
        setPhysInvMOITensor(RotationMatrices.inverse3by3(framedMOI));
    }

    protected void calculateForces() {
        applyAirDrag();
        applyGravity();

        // Collections.shuffle(activeForcePositions);

        Vector blockForce = new Vector();
        Vector inBodyWO = new Vector();
        Vector crossVector = new Vector();
        World worldObj = getParent().getWorldObj();

        if (PhysicsSettings.doPhysicsBlocks && getParent().areShipChunksFullyLoaded()) {
        	// We want to loop through all the physics nodes in a sorted order. Priority Queue handles that.
        	Queue<INodeController> nodesPriorityQueue = new PriorityQueue<INodeController>();
        	for (INodeController processor : parent.getPhysicsControllersInShip()) {
        		nodesPriorityQueue.add(processor);
        	}
        	
        	while (nodesPriorityQueue.size() > 0) {
        		INodeController controller = nodesPriorityQueue.poll();
        		controller.onPhysicsTick(parent, this, this.getPhysicsTimeDeltaPerPhysTick());
        	}
        	
        	SortedMap<IBlockTorqueProvider, List<BlockPos>> torqueProviders = new TreeMap<IBlockTorqueProvider, List<BlockPos>>();
            for (BlockPos pos : activeForcePositions) {
                IBlockState state = getParent().getShipChunks().getBlockState(pos);
                Block blockAt = state.getBlock();

				if (blockAt instanceof IBlockForceProvider) {
					try {
		                VWMath.getBodyPosWithOrientation(pos, physCenterOfMass, getParent().getShipTransformationManager()
		                        .getCurrentPhysicsTransform().getInternalMatrix(TransformType.SUBSPACE_TO_GLOBAL), inBodyWO);
						BlockForce.basicForces.getForceFromState(state, pos, worldObj, getPhysicsTimeDeltaPerPhysTick(),
								getParent(), blockForce);
						if (blockForce != null) {
							Vector otherPosition = ((IBlockForceProvider) blockAt).getCustomBlockForcePosition(worldObj,
									pos, state, getParent().getWrapperEntity(), getPhysicsTimeDeltaPerPhysTick());
							if (otherPosition != null) {
								VWMath.getBodyPosWithOrientation(otherPosition, gameTickCenterOfMass,
										getParent().getShipTransformationManager().getCurrentPhysicsTransform()
												.getInternalMatrix(TransformType.SUBSPACE_TO_GLOBAL),
										inBodyWO);
							}
	
							addForceAtPoint(inBodyWO, blockForce, crossVector);
							// Add particles here.
							if (IBlockForceProvider.class.cast(blockAt).doesForceSpawnParticles()) {
								// Dumb hack because addForceAtPoint changes the value of blockForce. Will be
								// removed soon.
								blockForce.multiply(1 / PHYSICS_SPEEDUP_FACTOR);
								// Dumb hack end.

								Vector particlePos;
								if (otherPosition != null) {
									particlePos = new Vector(otherPosition);
								} else {
									particlePos = new Vector(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
								}
								parent.getShipTransformationManager().getCurrentPhysicsTransform()
										.transform(particlePos, TransformType.SUBSPACE_TO_GLOBAL);
								// System.out.println(particlePos);
								float posX = (float) particlePos.X;
								float posY = (float) particlePos.Y;
								float posZ = (float) particlePos.Z;
								float particleMass = 5f;
								float velX = (float) -(blockForce.X / particleMass);
								float velY = (float) -(blockForce.Y / particleMass);
								float velZ = (float) -(blockForce.Z / particleMass);
								// Half a second
								float particleLife = .5f;
								// System.out.println(blockForce);
								// System.out.println(posX + ":" + posY + ":" + posZ);
								// System.out.println(velX + ":" + velY + ":" + velZ);

								this.particleManager.spawnPhysicsParticle(posX, posY, posZ, velX, velY, velZ,
										particleMass, particleLife);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (blockAt instanceof IBlockTorqueProvider) {
					// Add it to the torque sorted map; we do this so the torque dampeners can run
					// after the gyroscope stabilizers.
					IBlockTorqueProvider torqueProviderBlock = (IBlockTorqueProvider) blockAt;
					if (!torqueProviders.containsKey(torqueProviderBlock)) {
						torqueProviders.put(torqueProviderBlock, new LinkedList<BlockPos>());
					}
					torqueProviders.get(torqueProviderBlock).add(pos);
				}
            }
            
			// Now add the torque from the torque providers, in a sorted order!
			for (IBlockTorqueProvider torqueProviderBlock : torqueProviders.keySet()) {
				List<BlockPos> blockPositions = torqueProviders.get(torqueProviderBlock);
				for (BlockPos pos : blockPositions) {
					this.convertTorqueToVelocity();
					Vector torqueVector = torqueProviderBlock.getTorqueInGlobal(this, pos);
					if (torqueVector != null) {
						torque.add(torqueVector);
					}
				}
			}
        }
        particleManager.physicsTickAfterAllPreForces((float) getPhysicsTimeDeltaPerPhysTick());
        
        convertTorqueToVelocity();
    }

    public void applyGravity() {
        if (PhysicsSettings.doGravity) {
            addForceAtPoint(new Vector(0, 0, 0),
                    ValkyrienWarfareMod.gravity.getProduct(gameTickMass * getPhysicsTimeDeltaPerPhysTick()));
        }
    }

    public void calculateForcesArchimedes() {
        applyAirDrag();
    }

    protected void applyAirDrag() {
        double drag = getDragForPhysTick();
        linearMomentum.multiply(drag);
        angularVelocity.multiply(drag);
    }

    public void convertTorqueToVelocity() {
        if (!torque.isZero()) {
            angularVelocity.add(RotationMatrices.get3by3TransformedVec(getPhysInvMOITensor(), torque));
            torque.zero();
        }
    }

    public void addForceAtPoint(Vector inBodyWO, Vector forceToApply) {
        forceToApply.multiply(PHYSICS_SPEEDUP_FACTOR);
        torque.add(inBodyWO.cross(forceToApply));
        linearMomentum.add(forceToApply);
    }

    public void addForceAtPoint(Vector inBodyWO, Vector forceToApply, Vector crossVector) {
        forceToApply.multiply(PHYSICS_SPEEDUP_FACTOR);
        crossVector.setCross(inBodyWO, forceToApply);
        torque.add(crossVector);
        linearMomentum.add(forceToApply);
    }

    public void updatePhysSpeedAndIters(double newPhysSpeed) {
        physTickTimeDelta = newPhysSpeed;
    }

    public void applyAngularVelocity() {
        ShipTransformationManager coordTrans = getParent().getShipTransformationManager();
        
        double[] rotationChange = RotationMatrices.getRotationMatrix(angularVelocity.X, angularVelocity.Y,
                angularVelocity.Z, angularVelocity.length() * getPhysicsTimeDeltaPerPhysTick());
        Quaternion finalTransform = Quaternion.QuaternionFromMatrix(RotationMatrices.getMatrixProduct(rotationChange,
                coordTrans.getCurrentPhysicsTransform().getInternalMatrix(TransformType.SUBSPACE_TO_GLOBAL)));

        double[] radians = finalTransform.toRadians();

        physPitch = Double.isNaN(radians[0]) ? 0.0f : (float) Math.toDegrees(radians[0]);
        physYaw = Double.isNaN(radians[1]) ? 0.0f : (float) Math.toDegrees(radians[1]);
        physRoll = Double.isNaN(radians[2]) ? 0.0f : (float) Math.toDegrees(radians[2]);
    }

    public void applyLinearVelocity() {
        double momentMod = getPhysicsTimeDeltaPerPhysTick() * getInvMass();

        physX += (linearMomentum.X * momentMod);
        physY += (linearMomentum.Y * momentMod);
        physZ += (linearMomentum.Z * momentMod);
        physY = Math.min(Math.max(physY, ValkyrienWarfareMod.shipLowerLimit), ValkyrienWarfareMod.shipUpperLimit);
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
        compound.setDouble("mass", gameTickMass);

        NBTUtils.writeVectorToNBT("linear", linearMomentum, compound);
        NBTUtils.writeVectorToNBT("angularVelocity", angularVelocity, compound);
        NBTUtils.writeVectorToNBT("CM", gameTickCenterOfMass, compound);

        NBTUtils.write3x3MatrixToNBT("MOI", gameMoITensor, compound);
    }

    public void readFromNBTTag(NBTTagCompound compound) {
        gameTickMass = compound.getDouble("mass");

        linearMomentum = NBTUtils.readVectorFromNBT("linear", compound);
        angularVelocity = NBTUtils.readVectorFromNBT("angularVelocity", compound);
        gameTickCenterOfMass = NBTUtils.readVectorFromNBT("CM", compound);

        gameMoITensor = NBTUtils.read3x3MatrixFromNBT("MOI", compound);
    }

    // Called upon a Ship being created from the World, and generates the physics
    // data for it
    public void processInitialPhysicsData() {
        IBlockState air = Blocks.AIR.getDefaultState();
        for (BlockPos pos : getParent().getBlockPositions()) {
            onSetBlockState(air, getParent().getShipChunks().getBlockState(pos), pos);
        }
	}

	// These getter methods guarantee that only code within this class can modify
	// the mass, preventing outside code from breaking things
	public double getMass() {
		return gameTickMass;
	}

    public double getInvMass() {
        return 1D / gameTickMass;
    }

    public double getPhysicsTimeDeltaPerPhysTick() {
        return physTickTimeDelta;
    }

    public double getDragForPhysTick() {
        return Math.pow(DRAG_CONSTANT, getPhysicsTimeDeltaPerPhysTick() * 20D);
    }

    public void addPotentialActiveForcePos(BlockPos pos) {
        this.activeForcePositions.add(pos);
    }

    /**
     * @return The inverse moment of inertia tensor with local translation (0 vector
     * is at the center of mass), but rotated into world coordinates.
     */
    public double[] getPhysInvMOITensor() {
        return physInvMOITensor;
    }

    /**
     * @param physInvMOITensor the physInvMOITensor to set
     */
    private void setPhysInvMOITensor(double[] physInvMOITensor) {
        this.physInvMOITensor = physInvMOITensor;
    }

    /**
     * @return The moment of inertia tensor with local translation (0 vector is at
     * the center of mass), but rotated into world coordinates.
     */
    public double[] getPhysMOITensor() {
        return this.physMOITensor;
    }

	/**
	 * @return the parent
	 */
	public PhysicsObject getParent() {
		return parent;
	}

	/**
	 * @return the worldCollision
	 */
	public WorldPhysicsCollider getWorldCollision() {
		return worldCollision;
	}

	public double getInertiaAlongRotationAxis() {
		Vector rotationAxis = new Vector(angularVelocity);
		rotationAxis.normalize();
		RotationMatrices.applyTransform3by3(getPhysMOITensor(), rotationAxis);
		return rotationAxis.length();
	}
	
	@Deprecated
	public Vector getCopyOfPhysCoordinates() {
		return new Vector(physX, physY, physZ);
	}

}
