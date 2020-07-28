package org.valkyrienskies.addon.control.gui;

import static org.valkyrienskies.addon.control.gui.VS_Gui_Enum.PHYSICS_INFUSER;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import org.valkyrienskies.addon.control.container.ContainerPhysicsInfuser;
import org.valkyrienskies.addon.control.tileentity.TileEntityPhysicsInfuser;

public class VSGuiHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player,
        World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

        if (ID == PHYSICS_INFUSER.ordinal() && tileEntity instanceof TileEntityPhysicsInfuser) {
            return new ContainerPhysicsInfuser(player, (TileEntityPhysicsInfuser) tileEntity);
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player,
        World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

        if (ID == PHYSICS_INFUSER.ordinal() && tileEntity instanceof TileEntityPhysicsInfuser) {
            return new GuiPhysicsInfuser(player, (TileEntityPhysicsInfuser) tileEntity);
        }

        return null;
    }
}