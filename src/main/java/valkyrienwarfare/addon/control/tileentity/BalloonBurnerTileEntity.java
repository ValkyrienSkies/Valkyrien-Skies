/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2017 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package valkyrienwarfare.addon.control.tileentity;

import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;
import valkyrienwarfare.addon.control.balloon.BalloonProcessor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BalloonBurnerTileEntity extends TileEntity implements ITickable {

	public BalloonProcessor balloon;

	public BalloonBurnerTileEntity() {
		validate();
	}

	public Vector getBlockForcePosition(World world, BlockPos pos, IBlockState state, Entity shipEntity, double secondsToApply) {
		if (balloon == null) {
			PhysicsWrapperEntity wrapper = (PhysicsWrapperEntity) shipEntity;
			balloon = wrapper.wrapping.balloonManager.getProcessorAbovePos(pos);
		}
		if (balloon != null) {
			return balloon.currentBalloonCenter;
		}
		return null;
	}

	public Vector getBlockForce(World world, BlockPos pos, IBlockState state, Entity shipEntity, double secondsToApply) {
		if (balloon != null) {
			Vector upForce = new Vector(0D, 10D, 0D);
			upForce.multiply(balloon.currentBalloonSize);
			return upForce;
		}
		return null;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		return super.writeToNBT(compound);
	}

	@Override
	public void update() {
		// System.out.println("works");

	}

}