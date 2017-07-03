package ValkyrienWarfareControl.Block;

import java.util.List;

import ValkyrienWarfareControl.ValkyrienWarfareControlMod;
import ValkyrienWarfareControl.GUI.ControlGUIEnum;
import ValkyrienWarfareControl.TileEntity.ThrustModulatorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class BlockThrustModulator extends Block implements ITileEntityProvider {

    public BlockThrustModulator(Material materialIn) {
        super(materialIn);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    	TileEntity modulatorTile = worldIn.getTileEntity(pos);
    	if(modulatorTile != null) {
    		if(playerIn.getHeldItem(hand).item == ValkyrienWarfareControlMod.instance.relayWire){
    			return false;
    		}
	    	if(worldIn.isRemote) {
	    		playerIn.openGui(ValkyrienWarfareControlMod.instance, ControlGUIEnum.ThrustModulatorGUI.ordinal(), worldIn, pos.getX(), pos.getY(), pos.getZ());
	    	}else {
	    		((EntityPlayerMP) playerIn).connection.sendPacket(modulatorTile.getUpdatePacket());
	    	}
	    	return true;
    	}
    	return false;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) {
        itemInformation.add(TextFormatting.ITALIC + "" + TextFormatting.RED + TextFormatting.ITALIC + "Unfinished until v_0.91_alpha");
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new ThrustModulatorTileEntity();
    }

}
