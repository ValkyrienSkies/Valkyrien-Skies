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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.control.block.BlockShipHelm;
import valkyrienwarfare.addon.control.piloting.ControllerInputType;
import valkyrienwarfare.addon.control.piloting.PilotControlsMessage;
import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

public class TileEntityShipHelm extends ImplTileEntityPilotable implements ITickable {

    public double compassAngle = 0;
    public double lastCompassAngle = 0;

    public double wheelRotation = 0;
    public double lastWheelRotation = 0;

    double nextWheelRotation;

    @Override
    public void update() {
        if (this.getWorld().isRemote) {
            calculateCompassAngle();

            lastWheelRotation = wheelRotation;
            wheelRotation = nextWheelRotation;
        } else {
            double toOriginRate = 5D;
            if (Math.abs(wheelRotation) < toOriginRate) {
                wheelRotation = 0;
            } else {
//            	wheelRotation -= math.signum(wheelRotation) * wheelRotation;

                wheelRotation += -Math.signum(wheelRotation) * toOriginRate;
            }

            sendUpdatePacketToAllNearby();
        }
    }

    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.SPacketUpdateTileEntity pkt) {
//		lastWheelRotation = wheelRotation;

        nextWheelRotation = pkt.getNbtCompound().getDouble("wheelRotation");
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tagToSend = new NBTTagCompound();
        tagToSend.setDouble("wheelRotation", wheelRotation);
        return new SPacketUpdateTileEntity(this.getPos(), 0, tagToSend);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound toReturn = super.getUpdateTag();
        toReturn.setDouble("wheelRotation", wheelRotation);
        return toReturn;
    }

    public void calculateCompassAngle() {
        lastCompassAngle = compassAngle;

        IBlockState helmState = getWorld().getBlockState(getPos());
        EnumFacing enumfacing = helmState.getValue(BlockShipHelm.FACING);
        double wheelAndCompassStateRotation = enumfacing.getHorizontalAngle();

        BlockPos spawnPos = getWorld().getSpawnPoint();
        Vector compassPoint = new Vector(getPos().getX(), getPos().getY(), getPos().getZ());
        compassPoint.add(1D, 2D, 1D);

        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(getWorld(), getPos());
        if (wrapper != null) {
            RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, compassPoint);
        }

        Vector compassDirection = new Vector(compassPoint);
        compassDirection.subtract(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());

        if (wrapper != null) {
            RotationMatrices.doRotationOnly(wrapper.wrapping.coordTransform.wToLTransform, compassDirection);
        }

        compassDirection.normalize();
        compassAngle = Math.toDegrees(Math.atan2(compassDirection.X, compassDirection.Z)) - wheelAndCompassStateRotation;
        compassAngle = (compassAngle + 360D) % 360D;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        lastWheelRotation = wheelRotation = compound.getDouble("wheelRotation");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound toReturn = super.writeToNBT(compound);

        compound.setDouble("wheelRotation", wheelRotation);

        return toReturn;
    }

    @Override
    ControllerInputType getControlInputType() {
        return ControllerInputType.ShipHelm;
    }

    @Override
    boolean setClientPilotingEntireShip() {
        return false;
    }

    @Override
    void processControlMessage(PilotControlsMessage message, EntityPlayerMP sender) {
//		System.out.println("We Gotem!");
        if (message.airshipLeft_KeyDown) {
            wheelRotation -= 10D;
        }
        if (message.airshipRight_KeyDown) {
            wheelRotation += 10D;
        }
    }

}
