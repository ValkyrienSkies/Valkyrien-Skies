package org.valkyrienskies.addon.control.block;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.valkyrienskies.addon.control.nodenetwork.IVSNode;
import org.valkyrienskies.addon.control.tileentity.TileEntityNetworkDisplay;
import org.valkyrienskies.addon.control.util.BaseBlock;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockNetworkDisplay extends BaseBlock implements ITileEntityProvider {

    public BlockNetworkDisplay() {
        super("network_display", Material.IRON, 0.0F, true);
        this.setHardness(5.0F);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player,
        List<String> itemInformation,
        ITooltipFlag advanced) {
        itemInformation.add(TextFormatting.BLUE + I18n.format("tooltip.vs_control.network_display"));
    }


    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state,
        EntityPlayer playerIn,
        EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            TileEntity tile = worldIn.getTileEntity(pos);
            if (tile instanceof TileEntityNetworkDisplay) {
                TileEntityNetworkDisplay displayTile = (TileEntityNetworkDisplay) tile;
                Iterable<IVSNode> networkedObjects = displayTile.getNetworkedConnections();
                List<IVSNode> connectedNodes = new ArrayList<IVSNode>();
                Map<String, Integer> networkedClassTypeCounts = new HashMap<String, Integer>();
                for (IVSNode node : networkedObjects) {
                    connectedNodes.add(node);
                    Class nodeClass = node.getParentTile().getClass();
                    String tileClassName = nodeClass.getSimpleName();
                    if (!networkedClassTypeCounts.containsKey(tileClassName)) {
                        networkedClassTypeCounts.put(tileClassName, 0);
                    }
                    networkedClassTypeCounts
                        .put(tileClassName, networkedClassTypeCounts.get(tileClassName) + 1);
                }
                playerIn.sendMessage(new TextComponentString(
                    "Networked objects connected: " + connectedNodes.size()));
                playerIn.sendMessage(new TextComponentString(
                    "Types of objects connected: " + networkedClassTypeCounts.toString()));
            }
        }
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityNetworkDisplay();
    }

}
