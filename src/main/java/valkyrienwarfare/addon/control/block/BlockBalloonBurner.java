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

package valkyrienwarfare.addon.control.block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.control.balloon.BalloonDetector;
import valkyrienwarfare.addon.control.balloon.BalloonProcessor;
import valkyrienwarfare.addon.control.tileentity.BalloonBurnerTileEntity;
import valkyrienwarfare.physicsmanagement.PhysicsObject;
import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;

import javax.annotation.Nullable;
import java.util.List;

public class BlockBalloonBurner extends Block implements ITileEntityProvider {

    public BlockBalloonBurner(Material materialIn) {
        super(materialIn);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        PhysicsWrapperEntity wrapperEntity = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(worldIn, pos);
        // Balloons can only be made on an active Ship
        if (wrapperEntity != null) {
            BlockPos balloonStart = pos.up(2);
            if (!worldIn.isRemote) {
                BalloonProcessor existingProcessor = wrapperEntity.wrapping.balloonManager.getProcessorAbovePos(pos);
                if (existingProcessor == null) {

                    BalloonDetector detector = new BalloonDetector(balloonStart, worldIn, 25000);
                    int balloonSize = detector.foundSet.size();
                    if (balloonSize == 0) {
                        placer.sendMessage(new TextComponentString("No balloon above"));
                    } else {
                        placer.sendMessage(new TextComponentString("Created a new balloon"));

                        BalloonProcessor processor = BalloonProcessor.makeProcessorForDetector(wrapperEntity, detector);

                        wrapperEntity.wrapping.balloonManager.addBalloonProcessor(processor);
                        // System.out.println("balloon Walls Are " + detector.balloonWalls.size());
                    }
                } else {
                    placer.sendMessage(new TextComponentString("Hooked onto Exisiting balloon"));
                }
            }
        }
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> itemInformation, ITooltipFlag advanced) {
        itemInformation.add(TextFormatting.ITALIC + "" + TextFormatting.RED + TextFormatting.ITALIC + "Unfinished until v_0.91_alpha");
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new BalloonBurnerTileEntity();
    }

    private BalloonBurnerTileEntity getTileEntity(World world, BlockPos pos, IBlockState state, Entity shipEntity) {
        PhysicsWrapperEntity wrapper = (PhysicsWrapperEntity) shipEntity;
        PhysicsObject obj = wrapper.wrapping;
        IBlockState controllerState = obj.VKChunkCache.getBlockState(pos);
        TileEntity worldTile = obj.VKChunkCache.getTileEntity(pos);
        if (worldTile == null) {
            return null;
        }
        if (worldTile instanceof BalloonBurnerTileEntity) {
            BalloonBurnerTileEntity burnerTile = (BalloonBurnerTileEntity) worldTile;
            return burnerTile;
        }
        return null;
    }

}
