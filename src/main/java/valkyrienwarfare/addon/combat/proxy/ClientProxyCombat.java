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

package valkyrienwarfare.addon.combat.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import valkyrienwarfare.addon.combat.ValkyrienWarfareCombat;
import valkyrienwarfare.addon.combat.entity.EntityCannonBall;
import valkyrienwarfare.addon.combat.entity.EntityCannonBasic;
import valkyrienwarfare.addon.combat.render.EntityCannonBallRenderer;
import valkyrienwarfare.addon.combat.render.EntityCannonBasicRender;
import valkyrienwarfare.addon.combat.render.EntityCannonBasicRenderFactory;

public class ClientProxyCombat extends CommonProxyCombat {

    @Override
    public void preInit(FMLStateEvent e) {
        super.preInit(e);
        OBJLoader.INSTANCE.addDomain(ValkyrienWarfareCombat.INSTANCE.getModID().toLowerCase());
        RenderingRegistry.registerEntityRenderingHandler(EntityCannonBasic.class, new EntityCannonBasicRenderFactory());
        RenderingRegistry.registerEntityRenderingHandler(EntityCannonBall.class, new EntityCannonBasicRenderFactory.EntityCannonBallRenderFactory());
    }

    @Override
    public void init(FMLStateEvent e) {
        super.init(e);
    }

    @Override
    public void postInit(FMLStateEvent e) {
        super.postInit(e);
        registerItemModel(ValkyrienWarfareCombat.INSTANCE.basicCannonSpawner);
        registerItemModel(ValkyrienWarfareCombat.INSTANCE.cannonBall);
        registerItemModel(ValkyrienWarfareCombat.INSTANCE.powderPouch);
        registerItemModel(ValkyrienWarfareCombat.INSTANCE.explosiveArrow);

        EntityCannonBallRenderer.instance.cacheStates();
        EntityCannonBasicRender.instance.cacheStates();
    }

    private void registerItemModel(Item toRegister) {
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        renderItem.getItemModelMesher()
                .register(toRegister, 0, new ModelResourceLocation(ValkyrienWarfareCombat.INSTANCE.getModID() + ":" + toRegister.getTranslationKey()
                        .substring(5), "inventory"));
    }
}
