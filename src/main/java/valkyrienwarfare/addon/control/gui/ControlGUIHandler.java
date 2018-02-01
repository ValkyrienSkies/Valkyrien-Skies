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

package valkyrienwarfare.addon.control.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import valkyrienwarfare.addon.control.controlsystems.controlgui.ThrustModulatorGui;
import valkyrienwarfare.addon.control.tileentity.ThrustModulatorTileEntity;
import valkyrienwarfare.addon.control.tileentity.TileEntityHoverController;

public class ControlGUIHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == ControlGUIEnum.HoverCraftController.ordinal()) {
            TileEntity tileEnt = world.getTileEntity(new BlockPos(x, y, z));
            if (!(tileEnt instanceof TileEntityHoverController)) {
                return null;
            }
            TileEntityHoverController tile = (TileEntityHoverController) tileEnt;
            ((EntityPlayerMP) player).connection.sendPacket(tile.getUpdatePacket());
            return new HovercraftControllerContainer(player.inventory, tile);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == ControlGUIEnum.HoverCraftController.ordinal()) {
            return new HovercraftControllerGUI(player, (TileEntityHoverController) world.getTileEntity(new BlockPos(x, y, z)));
        }
        if (ID == ControlGUIEnum.ThrustModulatorGUI.ordinal()) {
            return new ThrustModulatorGui(player, (ThrustModulatorTileEntity) world.getTileEntity(new BlockPos(x, y, z)));
        }
        return null;
    }

}