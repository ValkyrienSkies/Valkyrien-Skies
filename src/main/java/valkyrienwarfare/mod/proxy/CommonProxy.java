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

package valkyrienwarfare.mod.proxy;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.api.addons.Module;
import valkyrienwarfare.api.addons.ModuleProxy;
import valkyrienwarfare.mod.event.EventsCommon;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent e) {
        for (Module addon : ValkyrienWarfareMod.addons) {
            ModuleProxy proxy = addon.getCommonProxy();
            if (proxy != null) {
                proxy.preInit(e);
            }
        }
    }

    public void init(FMLInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(new EventsCommon());

        for (Module addon : ValkyrienWarfareMod.addons) {
            ModuleProxy proxy = addon.getCommonProxy();
            if (proxy != null) {
                proxy.init(e);
            }
        }
    }

    public void postInit(FMLPostInitializationEvent e) {
        for (Module addon : ValkyrienWarfareMod.addons) {
            ModuleProxy proxy = addon.getCommonProxy();
            if (proxy != null) {
                proxy.postInit(e);
            }
        }
    }

    public void updateShipPartialTicks(PhysicsWrapperEntity wrapper) {

    }

    public void registerCommands(MinecraftServer server) {

    }

}
