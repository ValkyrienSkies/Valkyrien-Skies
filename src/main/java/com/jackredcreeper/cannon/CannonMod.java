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

package com.jackredcreeper.cannon;

import com.jackredcreeper.cannon.init.ModBlocks;
import com.jackredcreeper.cannon.init.ModItems;
import com.jackredcreeper.cannon.proxy.ClientProxy;
import com.jackredcreeper.cannon.tileentity.TileEntityCannon;
import com.jackredcreeper.cannon.world.ExplosionHandler;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import valkyrienwarfare.api.addons.Module;
import valkyrienwarfare.api.addons.VWAddon;

@VWAddon
public class CannonMod extends Module<CannonMod> {
    public static CannonMod instance;

    public CannonMod() {
        super(CannonModReference.MOD_ID, null, new ClientProxy(), null);
    }

    @Override
    public void registerItems(RegistryEvent.Register<Item> event) {
        ModItems.init();
        ModItems.register(event);
    }

    @Override
    public void registerItemBlocks(RegistryEvent.Register<Item> event) {
    }

    @Override
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        ModBlocks.init();
        ModBlocks.register(event);
    }

    @Override
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
    }

    @Override
    protected void registerEntities() {
    }

    @Override
    protected void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityCannon.class, CannonModReference.MOD_ID + "TileEntityCannon");
    }

    @Override
    protected void registerNetworks() {
    }

    @Override
    protected void registerCapabilities() {
    }

    @Override
    protected void preInit(FMLStateEvent event) {
    }

    @Override
    protected void init(FMLStateEvent event) {
        MinecraftForge.EVENT_BUS.register(new ExplosionHandler());
    }

    @Override
    protected void postInit(FMLStateEvent event) {

    }

    @Override
    public void applyConfig(Configuration config) {

    }
}
