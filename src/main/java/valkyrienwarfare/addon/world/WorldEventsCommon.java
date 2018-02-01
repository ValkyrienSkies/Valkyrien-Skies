/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2017 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package valkyrienwarfare.addon.world;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import valkyrienwarfare.PhysicsSettings;
import valkyrienwarfare.addon.world.block.BlockEtheriumOre;
import valkyrienwarfare.fixes.IInventoryPlayerFix;

public class WorldEventsCommon {

    @SubscribeEvent
    public void playerTick(PlayerTickEvent event) {
        if (event.phase == Phase.START) {
            EntityPlayer player = event.player;
            //TODO: fix the fall damage
            // @thebest108: what fall damage?
            //                    --DaPorkchop_, 28/03/2017
            if (PhysicsSettings.doEtheriumLifting) {
                if (!player.isCreative()) {
                    for (NonNullList<ItemStack> stackArray : IInventoryPlayerFix.getFixFromInventory(player.inventory).getAllInventories()) {
                        for (ItemStack stack : stackArray) {
                            if (stack != null) {
                                if (stack.getItem() instanceof ItemBlock) {
                                    ItemBlock blockItem = (ItemBlock) stack.getItem();
                                    if (blockItem.getBlock() instanceof BlockEtheriumOre) {
                                        player.addVelocity(0, .0025D * stack.stackSize, 0);
                                    }
                                } else if (stack.getItem() instanceof ItemEtheriumCrystal) {
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