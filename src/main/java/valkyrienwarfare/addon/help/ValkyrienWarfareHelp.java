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

package valkyrienwarfare.addon.help;

import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.help.item.ItemManual;
import valkyrienwarfare.addon.help.proxy.ClientProxyHelp;
import valkyrienwarfare.api.addons.Module;

/**
 * @author DaPorkchop_
 */
public class ValkyrienWarfareHelp extends Module {
    public static ValkyrienWarfareHelp INSTANCE;

    public Item manual;

    public ValkyrienWarfareHelp() {
        super("VW_Help", null, new ClientProxyHelp(), null);
        INSTANCE = this;
    }

    @Override
    public void applyConfig(Configuration config) {

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
        this.manual = new ItemManual().setUnlocalizedName("manual").setRegistryName(getModID(), "manual").setCreativeTab(ValkyrienWarfareMod.vwTab).setMaxStackSize(1);

        event.getRegistry().register(this.manual);
    }
}
