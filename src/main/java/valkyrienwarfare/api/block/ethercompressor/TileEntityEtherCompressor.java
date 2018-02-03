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
import valkyrienwarfare.addon.control.nodenetwork.BasicForceNodeTileEntity;
import valkyrienwarfare.addon.control.tileentity.TileEntityHoverController;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physics.management.PhysicsObject;

public abstract class TileEntityEtherCompressor extends BasicForceNodeTileEntity {

    public Vector linearThrust = new Vector();
    public Vector angularThrust = new Vector();
    //TODO: This is all temporary
    private BlockPos controllerPos;

    public TileEntityEtherCompressor() {
        validate();
    }

    public TileEntityEtherCompressor(Vector normalForceVector, double power) {
        super(normalForceVector, false, power);
        validate();
    }

    public BlockPos getControllerPos() {
        return controllerPos;
    }

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
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound toReturn = super.writeToNBT(compound);
        if (controllerPos != null) {
            toReturn.setInteger("controllerPosX", controllerPos.getX());
            toReturn.setInteger("controllerPosY", controllerPos.getY());
            toReturn.setInteger("controllerPosZ", controllerPos.getZ());
        }
        return toReturn;
    }

    @Override
    public boolean isForceOutputOriented() {
        return false;
    }

    //TODO: Remove this as soon as you can!
    @Override
    public Vector getForceOutputUnoriented(double secondsToApply) {
        if (controllerPos == null) {
            Vector output = super.getForceOutputUnoriented(secondsToApply);
//        	System.out.println(this.getMaxThrust());
            return output;
        }

        TileEntity controllerTile = world.getTileEntity(controllerPos);

        if (controllerTile != null) {

            if (controllerTile instanceof TileEntityHoverController) {
                TileEntityHoverController controller = (TileEntityHoverController) controllerTile;

                PhysicsObject physObj = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(world, pos).wrapping;

                Vector notToReturn = controller.getForceForEngine(this, world, getPos(), world.getBlockState(pos), physObj, secondsToApply);

                this.currentThrust = notToReturn.length() / secondsToApply;

//				System.out.println(currentThrust);

            }
        }
        return super.getForceOutputUnoriented(secondsToApply);
    }

}
