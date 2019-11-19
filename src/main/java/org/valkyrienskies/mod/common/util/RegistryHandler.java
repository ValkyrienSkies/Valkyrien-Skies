package org.valkyrienskies.mod.common.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.valkyrienskies.mod.client.BaseModel;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

@EventBusSubscriber
public class RegistryHandler {
	@SubscribeEvent
	public static void onItemRegister(RegistryEvent.Register<Item> event) {
		event.getRegistry().registerAll(ValkyrienSkiesMod.ITEMS.toArray(new Item[0]));
	}

	@SubscribeEvent
	public static void onBlockRegister(RegistryEvent.Register<Block> event) {
		Block[] blockArray = ValkyrienSkiesMod.BLOCKS.toArray(new Block[0]);
		event.getRegistry().registerAll(blockArray);
	}

	@SubscribeEvent
	public static void onModelRegister(ModelRegistryEvent event) {
		for (Item item : ValkyrienSkiesMod.ITEMS) {
			if (item instanceof BaseModel) {
				((BaseModel) item).registerModels();
			}
		}

		for (Block block : ValkyrienSkiesMod.BLOCKS) {
			if (block instanceof BaseModel) {
				((BaseModel) block).registerModels();
			}
		}
	}
}