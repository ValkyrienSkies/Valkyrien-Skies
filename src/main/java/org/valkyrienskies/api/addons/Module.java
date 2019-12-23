package org.valkyrienskies.api.addons;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

@Deprecated
public class Module {

    @Deprecated
    public static void registerRecipe(RegistryEvent.Register<IRecipe> event, String registryName,
        ItemStack out, Object... in) {
        CraftingHelper.ShapedPrimer primer = CraftingHelper.parseShaped(in);
        event.getRegistry()
            .register(new ShapedRecipes(ValkyrienSkiesMod.MOD_ID, primer.width, primer.height,
                primer.input, out).setRegistryName(ValkyrienSkiesMod.MOD_ID, registryName));
    }

    @Deprecated
    public static void registerItemBlock(RegistryEvent.Register<Item> event, Block block) {
        event.getRegistry().register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
    }

}
