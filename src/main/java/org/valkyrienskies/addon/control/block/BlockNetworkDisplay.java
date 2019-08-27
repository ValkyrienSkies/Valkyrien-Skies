package org.valkyrienskies.addon.control.block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.valkyrienskies.addon.control.nodenetwork.IVWNode;
import org.valkyrienskies.addon.control.tileentity.TileEntityNetworkDisplay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockNetworkDisplay extends Block implements ITileEntityProvider {

    public BlockNetworkDisplay(Material materialIn) {
        super(materialIn);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            TileEntity tile = worldIn.getTileEntity(pos);
            if (tile instanceof TileEntityNetworkDisplay) {
                TileEntityNetworkDisplay displayTile = (TileEntityNetworkDisplay) tile;
                Iterable<IVWNode> networkedObjects = displayTile.getNetworkedConnections();
                List<IVWNode> connectedNodes = new ArrayList<IVWNode>();
                Map<String, Integer> networkedClassTypeCounts = new HashMap<String, Integer>();
                for (IVWNode node : networkedObjects) {
                    connectedNodes.add(node);
                    Class nodeClass = node.getParentTile().getClass();
                    String tileClassName = nodeClass.getSimpleName();
                    if (!networkedClassTypeCounts.containsKey(tileClassName)) {
                        networkedClassTypeCounts.put(tileClassName, 0);
                    }
                    networkedClassTypeCounts.put(tileClassName, networkedClassTypeCounts.get(tileClassName) + 1);
                }
                playerIn.sendMessage(new TextComponentString("Networked objects connected: " + connectedNodes.size()));
                playerIn.sendMessage(new TextComponentString("Types of objects connected: " + networkedClassTypeCounts.toString()));
            }
        }
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityNetworkDisplay();
    }

}
