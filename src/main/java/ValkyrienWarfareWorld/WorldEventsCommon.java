package ValkyrienWarfareWorld;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class WorldEventsCommon {

	@SubscribeEvent
	public void playerTick(PlayerTickEvent event) {
		if(event.phase == Phase.START){
			EntityPlayer player = event.player;
			//TODO: Replace this with a GameRule or something
			//Also fix the fall damage
			if(!player.isCreative()){
				for(ItemStack[] stackArray: player.inventory.allInventories){
					for(ItemStack stack: stackArray){
						if(stack != null && stack.getItem() instanceof ItemBlock){
							ItemBlock blockItem = (ItemBlock) stack.getItem();
							if(blockItem.getBlock() instanceof BlockEtheriumOre){
								player.addVelocity(0, .0025D * stack.stackSize, 0);
							}
						}
					}
				}
			}
		}
	}
	
}
