/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.addon.combat;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.combat.entity.EntityCannonBall;
import valkyrienwarfare.addon.combat.entity.EntityCannonBasic;
import valkyrienwarfare.addon.combat.item.ItemBasicCannon;
import valkyrienwarfare.addon.combat.item.ItemCannonBall;
import valkyrienwarfare.addon.combat.item.ItemExplosiveArrow;
import valkyrienwarfare.addon.combat.item.ItemPowderPouch;
import valkyrienwarfare.addon.combat.proxy.ClientProxyCombat;
import valkyrienwarfare.addon.combat.proxy.CommonProxyCombat;
import valkyrienwarfare.api.addons.Module;
import valkyrienwarfare.api.addons.VWAddon;

@VWAddon
public class ValkyrienWarfareCombat extends Module {
    public static ValkyrienWarfareCombat INSTANCE;
    public Item basicCannonSpawner;
    public Item cannonBall;
    public Item powderPouch;
    public Item explosiveArrow;
    public Block fakecannonblock;

    public ValkyrienWarfareCombat() {
        super("VW_Combat", new CommonProxyCombat(), "valkyrienwarfarecombat");
        INSTANCE = this;
        if (ValkyrienWarfareMod.INSTANCE.isRunningOnClient()) {
            this.setClientProxy(new ClientProxyCombat());
        }
    }

    @Override
    protected void preInit(FMLStateEvent event) {
    }

    @Override
    protected void init(FMLStateEvent event) {
    }

    @Override
    protected void postInit(FMLStateEvent event) {
    }

    @Override
    public void registerItems(RegistryEvent.Register<Item> event) {
        basicCannonSpawner = new ItemBasicCannon().setUnlocalizedName("basiccannonspawner").setRegistryName(getModID(), "basiccannonspawner").setCreativeTab(ValkyrienWarfareMod.vwTab).setMaxStackSize(4);
        cannonBall = new ItemCannonBall().setUnlocalizedName("turretcannonball").setRegistryName(getModID(), "turretcannonball").setCreativeTab(ValkyrienWarfareMod.vwTab).setMaxStackSize(32);
        powderPouch = new ItemPowderPouch().setUnlocalizedName("powderpouch").setRegistryName(getModID(), "powderpouch").setCreativeTab(ValkyrienWarfareMod.vwTab).setMaxStackSize(32);
        explosiveArrow = new ItemExplosiveArrow().setUnlocalizedName("explosivearrow").setRegistryName(getModID(), "explosivearrow").setCreativeTab(ValkyrienWarfareMod.vwTab).setMaxStackSize(64);

        event.getRegistry().register(basicCannonSpawner);
        event.getRegistry().register(cannonBall);
        event.getRegistry().register(powderPouch);
        event.getRegistry().register(explosiveArrow);

        registerItemBlock(event, fakecannonblock);
    }

    @Override
    protected void registerEntities() {
        EntityRegistry.registerModEntity(new ResourceLocation(getModID(), "EntityCannonBasic"), EntityCannonBasic.class, "EntityCannonBasic", 71, ValkyrienWarfareMod.INSTANCE, 120, 1, false);
        EntityRegistry.registerModEntity(new ResourceLocation(getModID(), "EntityCannonBall"), EntityCannonBall.class, "EntityCannonBall", 72, ValkyrienWarfareMod.INSTANCE, 120, 5, true);
    }

    @Override
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        fakecannonblock = new FakeCannonBlock(Material.IRON).setHardness(5f).setUnlocalizedName("fakecannonblock").setRegistryName(getModID(), "fakecannonblock");

        event.getRegistry().register(fakecannonblock);
    }

    @Override
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        registerRecipe(event, new ItemStack(cannonBall, 4), "II ", "II ", "   ", 'I', Items.IRON_INGOT);
        registerRecipe(event, new ItemStack(powderPouch, 4), " S ", "SGS", " S ", 'S', Items.STRING, 'G', Items.GUNPOWDER);
    }

    @Override
    public void applyConfig(Configuration config) {

    }
}
