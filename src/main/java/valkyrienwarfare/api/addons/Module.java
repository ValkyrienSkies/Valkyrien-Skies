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

package valkyrienwarfare.api.addons;

import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import valkyrienwarfare.ValkyrienWarfareMod;

public abstract class Module<ImplName> {
    private String name;
    private boolean donePreInit = false, doneInit = false, donePostInit = false;
    private ModuleProxy common, client, server; //tODO: call these
    private String modid;

    public Module(String name, ModuleProxy common, ModuleProxy client, ModuleProxy server) {
        this(name, common, ValkyrienWarfareMod.MODID);
    }

    public Module(String name, ModuleProxy common, String modid) {
        this.name = name;
        this.common = common;
        this.modid = modid;
    }

    public static final void registerRecipe(RegistryEvent.Register<IRecipe> event, ItemStack out, Object... in) {
        CraftingHelper.ShapedPrimer primer = CraftingHelper.parseShaped(in);
        event.getRegistry().register(new ShapedRecipes(ValkyrienWarfareMod.MODID, primer.width, primer.height, primer.input, out).setRegistryName(ValkyrienWarfareMod.MODID, UUID.randomUUID().toString()));
    }

    public static final void registerItemBlock(RegistryEvent.Register<Item> event, Block block) {
        event.getRegistry().register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
    }

    public final void doPreInit(FMLStateEvent event) {
        if (!donePreInit) {
            setupConfig();
            registerEntities();
            registerCapabilities();
            preInit(event);
            donePreInit = true;
        }
    }

    public final void doInit(FMLStateEvent event) {
        if (!doneInit) {
            registerTileEntities();
            registerNetworks();
            init(event);
            doneInit = true;
        }
    }

    public final void doPostInit(FMLStateEvent event) {
        if (!donePostInit) {
            postInit(event);
            donePostInit = true;
        }
    }

    protected void setupConfig() {

    }

    public void registerItems(RegistryEvent.Register<Item> event) {

    }

    public void registerItemBlocks(RegistryEvent.Register<Item> event) {

    }

    public void registerBlocks(RegistryEvent.Register<Block> event) {

    }

    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {

    }

    protected void registerEntities() {

    }

    protected void registerTileEntities() {

    }

    protected void registerNetworks() {

    }

    protected void registerCapabilities() {
        
    }

    public final ModuleProxy getClientProxy() {
        return client;
    }

    public final void setClientProxy(ModuleProxy client) {
        this.client = client;
    }

    public final ModuleProxy getServerProxy() {
        return server;
    }

    public final void setServerProxy(ModuleProxy server) {
        this.server = server;
    }

    public final ModuleProxy getCommonProxy() {
        return common;
    }

    protected abstract void preInit(FMLStateEvent event);

    protected abstract void init(FMLStateEvent event);

    protected abstract void postInit(FMLStateEvent event);

    public final String getModID() {
        return modid;
    }
}
