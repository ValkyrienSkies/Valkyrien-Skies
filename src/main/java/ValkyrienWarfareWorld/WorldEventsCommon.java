package ValkyrienWarfareWorld;

import ValkyrienWarfareBase.PhysicsSettings;
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
			//TODO: fix the fall damage
			// @thebest108: what fall damage?
			//                    --DaPorkchop_, 28/03/2017
			if (PhysicsSettings.doEtheriumLifting) {
				if (!player.isCreative()) {
					for (ItemStack[] stackArray : player.inventory.allInventories) {
						for (ItemStack stack : stackArray) {
							if (stack != null) {
								if (stack.getItem() instanceof ItemBlock) {
									ItemBlock blockItem = (ItemBlock) stack.getItem();
									if (blockItem.getBlock() instanceof BlockEtheriumOre) {
										player.addVelocity(0, .0025D * stack.stackSize, 0);
									}
								} else if (stack.getItem() instanceof ItemEtheriumCrystal)	{
									player.addVelocity(0, .0025D * stack.stackSize, 0);
								}
							}
						}
					}
				}
			}
		}
	}
	
}
