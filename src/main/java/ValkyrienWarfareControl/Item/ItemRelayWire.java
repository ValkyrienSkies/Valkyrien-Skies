package ValkyrienWarfareControl.Item;

import java.util.List;

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
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemRelayWire extends Item {

    public static double range = 8D;

    public ItemRelayWire() {
        this.setMaxStackSize(1);
        this.setMaxDamage(80);
    }

    public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) {
        itemInformation.add(TextFormatting.ITALIC + "" + TextFormatting.RED + TextFormatting.ITALIC + "Unfinished until v_0.91_alpha");
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        IBlockState clickedState = worldIn.getBlockState(pos);
        Block block = clickedState.getBlock();

        TileEntity currentTile = worldIn.getTileEntity(pos);

        ItemStack stack = player.getHeldItem(hand);

        if (currentTile instanceof INodeProvider && !worldIn.isRemote) {
            ICapabilityLastRelay inst = stack.getCapability(ValkyrienWarfareControlMod.lastRelayCapability, null);
            if (inst != null) {
                if (!inst.hasLastRelay()) {

                    inst.setLastRelay(pos);
                    //Draw a wire in the player's hand after this
                } else {
                    BlockPos lastPos = inst.getLastRelay();
                    double distance = lastPos.distanceSq(pos);

                    TileEntity lastPosTile = worldIn.getTileEntity(lastPos);

//					System.out.println(lastPos.toString());

                    if (!lastPos.equals(pos) && lastPosTile != null && currentTile != null) {

                        if (distance < range * range) {
                            Node lastPosNode = ((INodeProvider) lastPosTile).getNode();
                            Node currentPosNode = ((INodeProvider) currentTile).getNode();
                            //Connect the two bastards
//							inst.setLastRelay(pos);
                            inst.setLastRelay(null);

                            if (lastPosNode != null && currentPosNode != null) {
                                currentPosNode.linkNode(lastPosNode);
                            }

//							System.out.println("Success");
                            stack.damageItem(1, player);
                        } else {
                            player.sendMessage(new TextComponentString("Nodes are too far away, try better wire"));
                            inst.setLastRelay(null);
                        }
                    } else {
                        inst.setLastRelay(pos);
                    }
                }
            }
        }

        if(currentTile instanceof INodeProvider) {
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.FAIL;
    }

}
