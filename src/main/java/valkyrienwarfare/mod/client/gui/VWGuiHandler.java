package valkyrienwarfare.mod.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import valkyrienwarfare.mod.container.ContainerPhysicsInfuser;
import valkyrienwarfare.mod.tileentity.TileEntityPhysicsInfuser;

import static valkyrienwarfare.mod.client.gui.VW_Gui_Enum.PHYSICS_INFUSER;

@SideOnly(Side.CLIENT)
public class VWGuiHandler implements IGuiHandler {

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