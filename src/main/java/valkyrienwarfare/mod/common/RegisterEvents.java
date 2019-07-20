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

package valkyrienwarfare.mod.common;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import valkyrienwarfare.mod.common.entity.EntityMountable;
import valkyrienwarfare.mod.common.entity.EntityMountableChair;
import valkyrienwarfare.mod.common.physics.management.PhysicsWrapperEntity;

import javax.annotation.Nonnull;

import static valkyrienwarfare.mod.common.ValkyrienWarfareMod.MOD_ID;
import static valkyrienwarfare.mod.common.ValkyrienWarfareMod.addons;

@Mod.EventBusSubscriber(modid = ValkyrienWarfareMod.MOD_ID)
public class RegisterEvents {
    @SubscribeEvent
    public static void registerBlocks(@Nonnull final RegistryEvent.Register<Block> event) {
        System.out.println("Registering blocks");
        ValkyrienWarfareMod.INSTANCE.registerBlocks(event);
        addons.forEach(m -> m.registerBlocks(event));
    }

    @SubscribeEvent
    public static void registerItems(@Nonnull final RegistryEvent.Register<Item> event) {
        System.out.println("Registering items");
        ValkyrienWarfareMod.INSTANCE.registerItems(event);
        addons.forEach(m -> {
            m.registerItems(event);
            m.registerItemBlocks(event);
        });
    }

    @SubscribeEvent
    public static void registerRecipes(@Nonnull final RegistryEvent.Register<IRecipe> event) {
        ValkyrienWarfareMod.INSTANCE.registerRecipes(event);
        addons.forEach(m -> m.registerRecipes(event));
    }

    /**
     * This method will be called by Forge when it is time for the mod to register its entity entries.
     */
    @SubscribeEvent
    public static void onRegisterEntitiesEvent(@Nonnull final RegistryEvent.Register<EntityEntry> event) {
        final ResourceLocation physicsWrapperEntity = new ResourceLocation(MOD_ID, "PhysWrapper");
        final ResourceLocation entityMountable = new ResourceLocation(MOD_ID, "entity_mountable");
        final ResourceLocation entityMountableChair = new ResourceLocation(MOD_ID, "entity_mountable_chair");
        // Used to ensure no duplicates of entity network id's
        int entityId = 0;
        event.getRegistry()
                .registerAll(EntityEntryBuilder.create()
                                .entity(PhysicsWrapperEntity.class)
                                .id(physicsWrapperEntity, entityId++)
                                .name(physicsWrapperEntity.getPath())
                                .tracker(ValkyrienWarfareMod.VW_ENTITY_LOAD_DISTANCE, 1, false)
                                .build(),
                        EntityEntryBuilder.create()
                                .entity(EntityMountable.class)
                                .id(entityMountable, entityId++)
                                .name(entityMountable.getPath())
                                .tracker(ValkyrienWarfareMod.VW_ENTITY_LOAD_DISTANCE, 1, false)
                                .build(),
                        EntityEntryBuilder.create()
                                .entity(EntityMountableChair.class)
                                .id(entityMountableChair, entityId++)
                                .name(entityMountableChair.getPath())
                                .tracker(ValkyrienWarfareMod.VW_ENTITY_LOAD_DISTANCE, 1, false)
                                .build()
                );
    }
}
