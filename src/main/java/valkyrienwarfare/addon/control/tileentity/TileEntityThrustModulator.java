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

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import valkyrienwarfare.addon.control.network.ThrustModulatorGuiInputMessage;
import valkyrienwarfare.addon.control.nodenetwork.VWNode_TileEntity;
import valkyrienwarfare.addon.control.proxy.ClientProxyControl;
import valkyrienwarfare.physics.PhysicsCalculations;
import valkyrienwarfare.physics.management.PhysicsObject;

@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")
public class TileEntityThrustModulator extends ImplNodeControllerTileEntity implements SimpleComponent {

    public static final int PHYSICS_PROCESSOR_PRIORITY = 100;
    public double idealYHeight = 25D;
    public double maximumYVelocity = 10D;

    // Initialize this ThrustModulatorTileEntity with a default priority of 100.
    public TileEntityThrustModulator() {
        super(PHYSICS_PROCESSOR_PRIORITY);
    }

    @Override
    public void onPhysicsTick(PhysicsObject object, PhysicsCalculations calculations, double secondsToSimulate) {
        // Confirmed working
        // System.out.println("debug1");
        // System.out.println(this.getNode().getNodeNetwork().getNetworkedNodes().size());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        ClientProxyControl.checkForTextFieldUpdate(this);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        idealYHeight = compound.getFloat("idealYHeight");
        maximumYVelocity = compound.getFloat("maximumYVelocity");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setFloat("idealYHeight", (float) idealYHeight);
        compound.setFloat("maximumYVelocity", (float) maximumYVelocity);
        return compound;
    }

    public void handleGUIInput(ThrustModulatorGuiInputMessage message, MessageContext ctx) {
        idealYHeight = Math.min(message.idealYHeight, 5000D);
        maximumYVelocity = Math.max(Math.min(message.maximumYVelocity, 100D), 0D);
        VWNode_TileEntity thisTileEntitiesNode = this.getNode();
        this.markDirty();
    }

    @Override
    public String getComponentName() {
        return "thrust_modulator";
    }

    @Callback
    @Optional.Method(modid = "opencomputers")
    public Object[] setYHeight(Context context, Arguments args) {
        boolean success = false;
        try {
            ThrustModulatorGuiInputMessage msg = new ThrustModulatorGuiInputMessage();
            msg.idealYHeight = (float) args.checkDouble(0);
            msg.maximumYVelocity = (float) maximumYVelocity;

            handleGUIInput(msg, null);
            success = true;
        } catch (IllegalArgumentException e) {
        }
        return new Object[]{success};
    }

    @Callback
    @Optional.Method(modid = "opencomputers")
    public Object[] setYVelocity(Context context, Arguments args) {
        boolean success = false;
        try {
            ThrustModulatorGuiInputMessage msg = new ThrustModulatorGuiInputMessage();
            msg.idealYHeight = (float) idealYHeight;
            msg.maximumYVelocity = (float) args.checkDouble(0);

            handleGUIInput(msg, null);
            success = true;
        } catch (IllegalArgumentException e) {
        }
        return new Object[]{success};
    }
}
