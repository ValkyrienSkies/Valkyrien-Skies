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

package valkyrienwarfare.addon.control.network;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import valkyrienwarfare.addon.control.tileentity.TileEntityThrustModulator;

public class ThrustModulatorGuiInputHandler implements IMessageHandler<ThrustModulatorGuiInputMessage, IMessage> {

    @Override
    public IMessage onMessage(ThrustModulatorGuiInputMessage message, MessageContext ctx) {
        IThreadListener mainThread = ctx.getServerHandler().serverController;
        mainThread.addScheduledTask(new Runnable() {
            @Override
            public void run() {
                TileEntity tileEnt = ctx.getServerHandler().player.world.getTileEntity(message.tileEntityPos);
                if (tileEnt != null) {
                    if (tileEnt instanceof TileEntityThrustModulator) {
                        ((TileEntityThrustModulator) tileEnt).handleGUIInput(message, ctx);
                    }
                } else {
                    System.out.println("Player: " + ctx.getServerHandler().player.getName() + " sent a broken packet");
                }
            }
        });
        return null;
    }

}
