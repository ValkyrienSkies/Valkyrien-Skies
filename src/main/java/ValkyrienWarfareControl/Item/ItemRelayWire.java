package ValkyrienWarfareControl.Item;

import ValkyrienWarfareControl.ValkyrienWarfareControlMod;
import ValkyrienWarfareControl.Capability.ICapabilityLastRelay;
import ValkyrienWarfareControl.NodeNetwork.INodeProvider;
import ValkyrienWarfareControl.NodeNetwork.Node;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ItemRelayWire extends Item {

	public static double range = 8D;

	public ItemRelayWire(){
		this.setMaxStackSize(1);
		this.setMaxDamage(80);
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		IBlockState clickedState = worldIn.getBlockState(pos);
		Block block = clickedState.getBlock();

		TileEntity currentTile = worldIn.getTileEntity(pos);

		if(currentTile instanceof INodeProvider && !worldIn.isRemote){
			ICapabilityLastRelay inst = stack.getCapability(ValkyrienWarfareControlMod.lastRelayCapability, null);
			if(inst != null){
				if(!inst.hasLastRelay()){

					inst.setLastRelay(pos);
					//Draw a wire in the player's hand after this
				}else{
					BlockPos lastPos = inst.getLastRelay();
					double distance = lastPos.distanceSq(pos);

					TileEntity lastPosTile = worldIn.getTileEntity(lastPos);

//					System.out.println(lastPos.toString());

					if(!lastPos.equals(pos) && lastPosTile != null && currentTile != null){

						if(distance < range * range){
							Node lastPosNode = ((INodeProvider) lastPosTile).getNode();
							Node currentPosNode = ((INodeProvider) currentTile).getNode();
							//Connect the two bastards
//							inst.setLastRelay(pos);
							inst.setLastRelay(null);

							if(lastPosNode != null && currentPosNode != null){
								currentPosNode.linkNode(lastPosNode);
							}

//							System.out.println("Success");
							stack.damageItem(1, playerIn);
						}else{
							playerIn.addChatComponentMessage(new TextComponentString("Nodes are too far away, try better wire"));
							inst.setLastRelay(null);
						}
					}else{
						inst.setLastRelay(pos);
					}
				}
			}
		}

		return EnumActionResult.FAIL;
	}

}
