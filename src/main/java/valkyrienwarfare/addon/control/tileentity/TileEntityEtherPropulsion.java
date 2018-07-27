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

import net.minecraft.nbt.NBTTagCompound;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.control.fuel.IEtherEngine;
import valkyrienwarfare.addon.control.nodenetwork.BasicForceNodeTileEntity;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.math.Vector;
import valkyrienwarfare.physics.management.PhysicsObject;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

public abstract class TileEntityEtherPropulsion extends BasicForceNodeTileEntity implements IEtherEngine {

	public TileEntityEtherPropulsion(Vector normalForceVector, double power) {
		super(normalForceVector, false, power);
		validate();
	}

	public TileEntityEtherPropulsion() {
		this(null, 0);
	}

	@Override
	public double getCurrentEtherEfficiency() {
		PhysicsWrapperEntity tilePhysics = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getObjectManagingPos(getWorld(), getPos());
		if (tilePhysics != null) {
			Vector tilePos = new Vector(getPos().getX() + .5D, getPos().getY() + .5D, getPos().getZ() + .5D);
			tilePhysics.getPhysicsObject().getShipTransformationManager().getCurrentPhysicsTransform().transform(tilePos, TransformType.SUBSPACE_TO_GLOBAL);
			double yPos = tilePos.Y;
			if (yPos < 0) {
				return 1;
			} else {
				double absoluteHeight = yPos + 50;
				double efficiency = 30 / absoluteHeight;
				efficiency = Math.max(0, Math.min(1, efficiency));
				return efficiency;
			}
		} else {
			return 1;
		}
	}
	
	/**
	 * Ether engines force output is not affected by the orientation of the engine,
	 * so the force vector is always unoriented.
	 */
	@Override
	public Vector getForceOutputOriented(double secondsToApply, PhysicsObject physicsObject) {
		return this.getForceOutputUnoriented(secondsToApply, physicsObject);
	}

	@Override
	public boolean isForceOutputOriented() {
		return false;
	}

}
