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

package com.jackredcreeper.cannon.init;

import com.jackredcreeper.cannon.items.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;

public class ModItems {

    public static Item key;
    public static Item loader;
    public static Item tuner;
    public static Item cannonball;
    public static Item explosiveball;
    public static Item grapeshot;
    public static Item solidball;

    public static void init() {
        key = new ItemPrimer();
        loader = new ItemLoader();
        tuner = new ItemTuner();
        cannonball = new ItemCannonball();
        explosiveball = new ItemExplosiveball();
        grapeshot = new ItemGrapeshot();
        solidball = new ItemSolidball();
    }

    public static void register(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(key);
        event.getRegistry().register(loader);
        event.getRegistry().register(tuner);
        event.getRegistry().register(cannonball);
        event.getRegistry().register(explosiveball);
        event.getRegistry().register(grapeshot);
        event.getRegistry().register(solidball);
    }

    public static void registerRenders() {
        registerRender(key);
        registerRender(loader);
        registerRender(tuner);
        registerRender(cannonball);
        registerRender(explosiveball);
        registerRender(grapeshot);
        registerRender(solidball);
    }

    private static void registerRender(Item item) {
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

}
