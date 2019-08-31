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

package org.valkyrienskies.addon.world;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import org.valkyrienskies.addon.world.block.BlockEthereumOre;
import org.valkyrienskies.addon.world.capability.AntiGravityCapabilityProvider;
import org.valkyrienskies.mod.common.config.VSConfig;

public class WorldEventsCommon {

    @SubscribeEvent
    public void onAttachCapabilityEventItem(AttachCapabilitiesEvent event) {
        if (event.getObject() instanceof ItemStack) {
            ItemStack stack = (ItemStack) event.getObject();
            if (stack.getItem() instanceof ItemEthereumCrystal) {
                event.addCapability(
                    new ResourceLocation(ValkyrienSkiesWorld.MOD_ID, "AntiGravityValue"),
                    new AntiGravityCapabilityProvider());
            }
        }
    }

    @SubscribeEvent
    public void worldTick(WorldTickEvent event) {
        if (event.phase == Phase.START) {
            for (Entity entity : event.world.loadedEntityList) {
                if (entity instanceof EntityItem) {
                    EntityItem itemEntity = (EntityItem) entity;
                    ItemStack itemStack = itemEntity.getItem();
                    if (itemStack
                        .hasCapability(ValkyrienSkiesWorld.ANTI_GRAVITY_CAPABILITY, null)) {
                        itemEntity.addVelocity(0, .1 - (itemEntity.motionY * .12D), 0);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void playerTick(PlayerTickEvent event) {
        if (event.phase == Phase.START) {
            EntityPlayer player = event.player;
            //TODO: fix the fall damage
            // @thebest108: what fall damage?
            //                    --DaPorkchop_, 28/03/2017
            if (VSConfig.doEthereumLifting) {
                if (!player.isCreative()) {
                    for (NonNullList<ItemStack> stackArray : player.inventory.allInventories) {
                        for (ItemStack stack : stackArray) {
                            if (stack != null) {
                                if (stack.getItem() instanceof ItemBlock) {
                                    ItemBlock blockItem = (ItemBlock) stack.getItem();
                                    if (blockItem.getBlock() instanceof BlockEthereumOre) {
                                        player.addVelocity(0, .0025D * stack.stackSize, 0);
                                    }
                                } else if (stack.getItem() instanceof ItemEthereumCrystal) {
                                    player.addVelocity(0, .0025D * stack.stackSize, 0);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}