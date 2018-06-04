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

package valkyrienwarfare.api.block.ethercompressor;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.control.fuel.IEtherGasEngine;
import valkyrienwarfare.addon.control.nodenetwork.BasicForceNodeTileEntity;
import valkyrienwarfare.addon.control.tileentity.TileEntityHoverController;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physics.management.PhysicsObject;

public abstract class TileEntityEtherCompressor extends BasicForceNodeTileEntity implements IEtherGasEngine {

	// These deprecated fields will be deleted at some point, but for now its best
	// to keep them around to maintain compatibility with older controls.
	@Deprecated
	private Vector linearThrust = new Vector();
	@Deprecated
	private Vector angularThrust = new Vector();
	@Deprecated
	private BlockPos controllerPos;
	private int etherGas;
	private int etherGasCapacity;

	public TileEntityEtherCompressor(Vector normalForceVector, double power) {
		super(normalForceVector, false, power);
		validate();
		etherGas = 0;
		etherGasCapacity = 1000;
	}

	public TileEntityEtherCompressor() {
		this(null, 0);
	}

	@Deprecated
	public BlockPos getControllerPos() {
		return controllerPos;
	}

	@Deprecated
	public void setControllerPos(BlockPos toSet) {
		controllerPos = toSet;
		this.markDirty();
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		int controllerPosX = compound.getInteger("controllerPosX");
		int controllerPosY = compound.getInteger("controllerPosY");
		int controllerPosZ = compound.getInteger("controllerPosZ");
		controllerPos = new BlockPos(controllerPosX, controllerPosY, controllerPosZ);
		etherGas = compound.getInteger("etherGas");
		etherGasCapacity = compound.getInteger("etherGasCapacity");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound toReturn = super.writeToNBT(compound);
		if (controllerPos != null) {
			toReturn.setInteger("controllerPosX", controllerPos.getX());
			toReturn.setInteger("controllerPosY", controllerPos.getY());
			toReturn.setInteger("controllerPosZ", controllerPos.getZ());
		}
		toReturn.setInteger("etherGas", etherGas);
		toReturn.setInteger("etherGasCapacity", etherGasCapacity);
		return toReturn;
	}

	@Override
	public boolean isForceOutputOriented() {
		return false;
	}

	@Override
	public Vector getForceOutputUnoriented(double secondsToApply, PhysicsObject physicsObject) {
		if (controllerPos == null) {
			Vector output = super.getForceOutputUnoriented(secondsToApply, physicsObject);
			return output;
		}
		// TODO: Causing physics crash with the Sponge
		// TileEntity controllerTile = world.getTileEntity(controllerPos);
		TileEntity controllerTile = physicsObject.shipChunks.getTileEntity(controllerPos);
		if (controllerTile != null) {
			if (controllerTile instanceof TileEntityHoverController) {
				TileEntityHoverController controller = (TileEntityHoverController) controllerTile;
				PhysicsObject physObj = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(world, pos)
						.getPhysicsObject();
				Vector notToReturn = controller.getForceForEngine(this, world, getPos(), world.getBlockState(pos),
						physObj, secondsToApply);
				this.currentThrust = notToReturn.length() / secondsToApply;
			}
		}
		return super.getForceOutputUnoriented(secondsToApply, physicsObject);
	}

	@Override
	public int getCurrentEtherGas() {
		return etherGas;
	}

	@Override
	public int getEtherGasCapacity() {
		return etherGasCapacity;
	}

	// pre : Throws an IllegalArgumentExcepion if more gas is added than there is
	// capacity for this engine.
	@Override
	public void addEtherGas(int gas) {
		if (etherGas + gas > etherGasCapacity) {
			throw new IllegalArgumentException();
		}
		etherGas += gas;
	}

	@Deprecated
	public Vector getLinearThrust() {
		return linearThrust;
	}

	@Deprecated
	public Vector getAngularThrust() {
		return angularThrust;
	}

}
