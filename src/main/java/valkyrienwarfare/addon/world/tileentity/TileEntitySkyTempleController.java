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

package valkyrienwarfare.addon.world.tileentity;

import javax.vecmath.Vector2d;

import net.minecraft.nbt.NBTTagCompound;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.control.tileentity.ImplPhysicsProcessorNodeTileEntity;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physics.calculations.PhysicsCalculations;
import valkyrienwarfare.physics.calculations.PhysicsCalculationsManualControl;
import valkyrienwarfare.physics.management.PhysicsObject;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;
import valkyrienwarfare.physics.management.ShipType;
import valkyrienwarfare.util.NBTUtils;

public class TileEntitySkyTempleController extends ImplPhysicsProcessorNodeTileEntity {

    double yawChangeRate = 8D;
    double yawPathRate = 2D;
    double yPathRate = 2D;
    double totalSecondsExisted = Math.random() * 15D;
    private Vector originPos = new Vector();
    private double orbitDistance;

    @Override
    public void onPhysicsTick(PhysicsObject object, PhysicsCalculations calculations, double secondsToSimulate) {
        if (calculations instanceof PhysicsCalculationsManualControl) {
            PhysicsCalculationsManualControl manualControl = (PhysicsCalculationsManualControl) calculations;

            ((PhysicsCalculationsManualControl) calculations).useLinearMomentumForce = true;

            if (originPos == null || originPos.isZero()) {
                setOriginPos(new Vector(object.wrapper.posX, object.wrapper.posY, object.wrapper.posZ));
            }

            manualControl.yawRate = yawChangeRate;

            Vector2d distanceFromCenter = new Vector2d(object.wrapper.posX - originPos.X, object.wrapper.posZ - originPos.Z);

            double realDist = distanceFromCenter.length();

            double invTan = Math.toDegrees(Math.atan2(distanceFromCenter.getY(), distanceFromCenter.getX()));

            double velocityAngle = invTan + 90D;

            double x = Math.cos(Math.toRadians(velocityAngle)) * yawPathRate;
            double z = Math.sin(Math.toRadians(velocityAngle)) * yawPathRate;

            if (realDist / orbitDistance > 1D) {
                double reductionFactor = (realDist / realDist) - 1D;

                x -= reductionFactor * distanceFromCenter.x * yawPathRate;
                z -= reductionFactor * distanceFromCenter.y * yawPathRate;

//				System.out.println(reductionFactor);
            }

            calculations.linearMomentum.X = x * calculations.getMass();
            calculations.linearMomentum.Z = z * calculations.getMass();

            totalSecondsExisted += secondsToSimulate;

            calculations.linearMomentum.Y = Math.sin(Math.toRadians(totalSecondsExisted * 7.5D)) * yPathRate;
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (!getWorld().isRemote) {
            PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(getWorld(), getPos());
            if (wrapper != null) {
                if (wrapper.wrapping.physicsProcessor instanceof PhysicsCalculationsManualControl) {
                    wrapper.wrapping.physicsProcessor = ((PhysicsCalculationsManualControl) wrapper.wrapping.physicsProcessor).downgradeToNormalCalculations();
                    wrapper.wrapping.setShipType(ShipType.Full_Unlocked);
                }
            }
        }
//		System.out.println("invalidated");
    }

    public void setOriginPos(Vector newPos) {
        originPos = newPos;
        //Minimum orbit of 40, maximum orbit of 100
        double orbitDistance = 40D + (Math.random() * 60D);
        //Assume we are at 0 degrees in our orbit
        originPos.X -= orbitDistance;

        this.markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        originPos = NBTUtils.readVectorFromNBT("originPos", compound);
        orbitDistance = compound.getDouble("orbitDistance");
        yawChangeRate = compound.getDouble("yawChangeRate");
        yawPathRate = compound.getDouble("yawPathRate");
        yPathRate = compound.getDouble("yPathRate");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        NBTUtils.writeVectorToNBT("originPos", originPos, compound);
        compound.setDouble("orbitDistance", orbitDistance);
        compound.setDouble("yawChangeRate", yawChangeRate);
        compound.setDouble("yawPathRate", yawPathRate);
        compound.setDouble("yPathRate", yPathRate);
        return compound;
    }

}
