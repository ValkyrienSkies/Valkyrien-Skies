package ValkyrienWarfareBase.Interaction;

import ValkyrienWarfareBase.Math.RotationMatrices;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketPlayerBlockPlacement;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;

public class CustomPlayerControllerMP extends PlayerControllerMP{

	public CustomPlayerControllerMP(Minecraft mcIn, NetHandlerPlayClient netHandler) {
		super(mcIn, netHandler);
	}

	public EnumActionResult processRightClickBlock(EntityPlayerSP player, WorldClient worldIn, ItemStack stack, BlockPos pos, EnumFacing facing, Vec3d vec, EnumHand hand)
    {
        this.syncCurrentPlayItem();
        float f = (float)(vec.xCoord - (double)pos.getX());
        float f1 = (float)(vec.yCoord - (double)pos.getY());
        float f2 = (float)(vec.zCoord - (double)pos.getZ());
        boolean flag = false;

        if (!this.mc.theWorld.getWorldBorder().contains(pos))
        {
            return EnumActionResult.FAIL;
        }
        else
        {
            net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock event = net.minecraftforge.common.ForgeHooks
                    .onRightClickBlock(player, hand, stack, pos, facing, net.minecraftforge.common.ForgeHooks.rayTraceEyeHitVec(player, getBlockReachDistance() + 1));
            if (event.isCanceled())
            {
                // Give the server a chance to fire event as well. That way server event is not dependant on client event.
                this.netClientHandler.addToSendQueue(new CPacketPlayerTryUseItem(pos, facing, hand, f, f1, f2));
                return EnumActionResult.PASS;
            }
            EnumActionResult result = EnumActionResult.PASS;

            if (this.currentGameType != WorldSettings.GameType.SPECTATOR)
            {
                net.minecraft.item.Item item = stack == null ? null : stack.getItem();
                EnumActionResult ret = item == null ? EnumActionResult.PASS : item.onItemUseFirst(stack, player, worldIn, pos, facing, f, f1, f2, hand);
                if (ret != EnumActionResult.PASS) return ret;

                IBlockState iblockstate = worldIn.getBlockState(pos);
                boolean bypass = true;
                for (ItemStack s : new ItemStack[]{player.getHeldItemMainhand(), player.getHeldItemOffhand()}) //TODO: Expand to more hands? player.inv.getHands()?
                    bypass = bypass && (s == null || s.getItem().doesSneakBypassUse(s, worldIn, pos, player));

                if (!player.isSneaking() || bypass || event.getUseBlock() == net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW)
                {
                    if(event.getUseBlock() != net.minecraftforge.fml.common.eventhandler.Event.Result.DENY)
                    flag = iblockstate.getBlock().onBlockActivated(worldIn, pos, iblockstate, player, hand, stack, facing, f, f1, f2);
                    if(flag) result = EnumActionResult.SUCCESS;
                }

                if (!flag && stack != null && stack.getItem() instanceof ItemBlock)
                {
                    ItemBlock itemblock = (ItemBlock)stack.getItem();

                    if (!itemblock.canPlaceBlockOnSide(worldIn, pos, facing, player, stack))
                    {
                        return EnumActionResult.FAIL;
                    }
                }
            }

            this.netClientHandler.addToSendQueue(new CPacketPlayerTryUseItem(pos, facing, hand, f, f1, f2));

            if (!flag && this.currentGameType != WorldSettings.GameType.SPECTATOR || event.getUseItem() == net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW)
            {
                if (stack == null)
                {
                    return EnumActionResult.PASS;
                }
                else if (player.getCooldownTracker().hasCooldown(stack.getItem()))
                {
                    return EnumActionResult.PASS;
                }
                else if (stack.getItem() instanceof ItemBlock && ((ItemBlock)stack.getItem()).getBlock() instanceof BlockCommandBlock && !player.canCommandSenderUseCommand(2, ""))
                {
                    return EnumActionResult.FAIL;
                }
                else if (this.currentGameType.isCreative())
                {
                    int i = stack.getMetadata();
                    int j = stack.stackSize;
                    if (event.getUseItem() != net.minecraftforge.fml.common.eventhandler.Event.Result.DENY) {
                    EnumActionResult enumactionresult = stack.onItemUse(player, worldIn, pos, hand, facing, f, f1, f2);
                    stack.setItemDamage(i);
                    stack.stackSize = j;
                    return enumactionresult;
                    } else return result;
                }
                else
                {
                    if (event.getUseItem() != net.minecraftforge.fml.common.eventhandler.Event.Result.DENY)
                    result = stack.onItemUse(player, worldIn, pos, hand, facing, f, f1, f2);
                    if (stack.stackSize <= 0) net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, stack, hand);
                    return result;
                }
            }
            else
            {
                return EnumActionResult.SUCCESS;
            }
        }
    }

    public EnumActionResult processRightClick(EntityPlayer player, World worldIn, ItemStack stack, EnumHand hand)
    {
        if (this.currentGameType == WorldSettings.GameType.SPECTATOR)
        {
            return EnumActionResult.PASS;
        }
        else
        {
            this.syncCurrentPlayItem();
            this.netClientHandler.addToSendQueue(new CPacketPlayerBlockPlacement(hand));

            if (player.getCooldownTracker().hasCooldown(stack.getItem()))
            {
                return EnumActionResult.PASS;
            }
            else
            {
                if (net.minecraftforge.common.ForgeHooks.onItemRightClick(player, hand, stack)) return net.minecraft.util.EnumActionResult.PASS;
                int i = stack.stackSize;
                ActionResult<ItemStack> actionresult = stack.useItemRightClick(worldIn, player, hand);
                ItemStack itemstack = (ItemStack)actionresult.getResult();

                if (itemstack != stack || itemstack.stackSize != i)
                {
                    player.setHeldItem(hand, itemstack);

                    if (itemstack.stackSize <= 0)
                    {
                        player.setHeldItem(hand, (ItemStack)null);
                        net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, itemstack, hand);
                    }
                }

                return actionresult.getType();
            }
        }
    }
	
	public static void clickBlockCreative(Minecraft mcIn, PlayerControllerMP playerController, BlockPos pos, EnumFacing facing)
    {
        if (!mcIn.theWorld.extinguishFire(mcIn.thePlayer, pos, facing))
        {
            playerController.func_187103_a(pos);
        }
    }

    //Called to start block breaking
    @Override
    public boolean clickBlock(BlockPos loc, EnumFacing face){
    	return super.clickBlock(loc, face);
    }

    /**
     * Resets current block damage and field_78778_j
     */
    @Override
    public void resetBlockRemoving(){      
    	super.resetBlockRemoving();
    }

    //Returns true if the hand should swing when breaking a block
    @Override
    public boolean onPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing){
    	return super.onPlayerDamageBlock(posBlock, directionFacing);
    }

    /**
     * Attacks an entity
     */
    @Override
    public void attackEntity(EntityPlayer playerIn, Entity targetEntity){
    	super.attackEntity(playerIn, targetEntity);
    }


    @Override
    public void onStoppedUsingItem(EntityPlayer playerIn){
    	super.onStoppedUsingItem(playerIn);
    }
    /**
     * true for hitting entities far away.
     */
    @Override
    public boolean extendedReach(){
        return super.extendedReach();
    }
	
}
