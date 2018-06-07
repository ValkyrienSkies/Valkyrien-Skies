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

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ITickable;
import valkyrienwarfare.addon.control.controlsystems.ShipTelegraphState;
import valkyrienwarfare.addon.control.piloting.ControllerInputType;
import valkyrienwarfare.addon.control.piloting.PilotControlsMessage;

public class TileEntityShipTelegraph extends ImplTileEntityPilotable implements ITickable {

    public ShipTelegraphState telegraphState = ShipTelegraphState.LANGSAM_1;
    public double oldHandleRotation;
    public double handleRotation;

    double nextHandleRotation;

    @Override
    void processControlMessage(PilotControlsMessage message, EntityPlayerMP sender) {
        int ordinal = telegraphState.ordinal();
        if (message.airshipLeft_KeyPressed && ordinal > 0) {
            handleRotation -= 22.5D;
            ordinal--;
        }
        if (message.airshipRight_KeyPressed && ordinal < 12) {
            handleRotation += 22.5D;
            ordinal++;
        }
//		ordinal = math.max(0, math.min(12, ordinal));
        telegraphState = ShipTelegraphState.values()[ordinal];
    }


    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.SPacketUpdateTileEntity pkt) {
//		lastWheelRotation = wheelRotation;

        nextHandleRotation = pkt.getNbtCompound().getDouble("handleRotation");
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tagToSend = new NBTTagCompound();
        tagToSend.setDouble("handleRotation", handleRotation);
        return new SPacketUpdateTileEntity(this.getPos(), 0, tagToSend);
    }

    public double getHandleRenderRotation() {
        return handleRotation;
    }

    @Override
    public void update() {
        if (getWorld().isRemote) {
            oldHandleRotation = handleRotation;
            handleRotation += (nextHandleRotation - handleRotation) * .35D;
        } else {
            sendUpdatePacketToAllNearby();
        }
//		this.markDirty();
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound toReturn = super.getUpdateTag();
        toReturn.setDouble("handleRotation", handleRotation);
        return toReturn;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        handleRotation = compound.getDouble("handleRotation");
        telegraphState = ShipTelegraphState.values()[compound.getInteger("telegraphStateOrdinal")];
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound toReturn = super.writeToNBT(compound);
        toReturn.setDouble("handleRotation", handleRotation);
        toReturn.setInteger("telegraphStateOrdinal", telegraphState.ordinal());
        return toReturn;
    }

    @Override
    ControllerInputType getControlInputType() {
        return ControllerInputType.Telegraph;
    }

    @Override
    boolean setClientPilotingEntireShip() {
        return false;
    }

}
