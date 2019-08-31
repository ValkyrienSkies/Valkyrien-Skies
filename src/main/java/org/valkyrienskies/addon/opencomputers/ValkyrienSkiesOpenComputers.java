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

package org.valkyrienskies.addon.opencomputers;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.valkyrienskies.addon.opencomputers.block.GPSBlock;
import org.valkyrienskies.addon.opencomputers.tileentity.GPSTileEntity;
import org.valkyrienskies.api.addons.Module;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

@Mod(
    name = ValkyrienSkiesOpenComputers.MOD_NAME,
    modid = ValkyrienSkiesOpenComputers.MOD_ID,
    version = ValkyrienSkiesOpenComputers.MOD_VERSION,
    dependencies = "required-after:" + ValkyrienSkiesMod.MOD_ID
)
public class ValkyrienSkiesOpenComputers {

    // MOD INFO CONSTANTS
    static final String MOD_ID = "vs_open_computers";
    static final String MOD_NAME = "Valkyrien Skies Open Computers";
    static final String MOD_VERSION = ValkyrienSkiesMod.MOD_VERSION;

    // MOD INSTANCE
    @Instance(MOD_ID)
    public static ValkyrienSkiesOpenComputers INSTANCE = new ValkyrienSkiesOpenComputers();

    // MOD CLASS MEMBERS
    private Block gpsBlock;

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        gpsBlock = new GPSBlock().setTranslationKey("gpsblock")
            .setRegistryName(MOD_ID, "gpsblock")
            .setCreativeTab(ValkyrienSkiesMod.vwTab);

        event.getRegistry().register(gpsBlock);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        Module.registerItemBlock(event, gpsBlock);
    }

    @SubscribeEvent
    public void registerTileEntities() {
        GameRegistry.registerTileEntity(GPSTileEntity.class, "tilegps");
    }
}
