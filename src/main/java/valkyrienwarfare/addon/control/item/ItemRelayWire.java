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

package valkyrienwarfare.addon.control.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;
import valkyrienwarfare.addon.control.capability.ICapabilityLastRelay;
import valkyrienwarfare.addon.control.nodenetwork.INodeProvider;
import valkyrienwarfare.addon.control.nodenetwork.Node;

public class ItemRelayWire extends Item {

    public static double range = 8D;

    public ItemRelayWire() {
        this.setMaxStackSize(1);
        this.setMaxDamage(80);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> itemInformation, ITooltipFlag advanced) {
        itemInformation.add(TextFormatting.ITALIC + "" + TextFormatting.RED + TextFormatting.ITALIC + "Unfinished until v_0.91_alpha");
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        IBlockState clickedState = worldIn.getBlockState(pos);
        Block block = clickedState.getBlock();
        
        TileEntity currentTile = worldIn.getTileEntity(pos);

        ItemStack stack = player.getHeldItem(hand);

        if (currentTile instanceof INodeProvider && !worldIn.isRemote) {
            ICapabilityLastRelay inst = stack.getCapability(ValkyrienWarfareControl.lastRelayCapability, null);
            if (inst != null) {
                
                if (!inst.hasLastRelay()) {

                    inst.setLastRelay(pos);
                    //Draw a wire in the player's hand after this
                } else {
                    BlockPos lastPos = inst.getLastRelay();
                    double distance = lastPos.distanceSq(pos);

                    TileEntity lastPosTile = worldIn.getTileEntity(lastPos);

//					System.out.println(lastPos.toString());

                    if (!lastPos.equals(pos) && lastPosTile != null && currentTile != null) {

                        if (distance < range * range) {
                            Node lastPosNode = ((INodeProvider) lastPosTile).getNode();
                            Node currentPosNode = ((INodeProvider) currentTile).getNode();
                            //Connect the two bastards
//							inst.setLastRelay(pos);
                            inst.setLastRelay(null);

                            if (lastPosNode != null && currentPosNode != null) {
                                if (currentPosNode.canLinkToNode(lastPosNode)) {
                                    currentPosNode.linkNode(lastPosNode);
                                } else {
                                    player.sendMessage(new TextComponentString("One of the connections must be to a thrust relay node"));
                                    inst.setLastRelay(null);
                                }
                            }

//							System.out.println("Success");
                            stack.damageItem(1, player);
                        } else {
                            player.sendMessage(new TextComponentString("Nodes are too far away, try better wire"));
                            inst.setLastRelay(null);
                        }
                    } else {
                        inst.setLastRelay(pos);
                    }
                }
            }
        }

        if (currentTile instanceof INodeProvider) {
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.FAIL;
    }

}
