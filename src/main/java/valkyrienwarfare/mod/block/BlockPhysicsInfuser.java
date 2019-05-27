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

package valkyrienwarfare.mod.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.mod.client.gui.VW_Gui_Enum;
import valkyrienwarfare.mod.physmanagement.relocation.DetectorManager;
import valkyrienwarfare.mod.tileentity.TileEntityPhysicsInfuser;

import javax.annotation.Nullable;
import java.util.List;

public class BlockPhysicsInfuser extends BlockVWDirectional {

    int shipSpawnDetectorID;

    public BlockPhysicsInfuser(Material materialIn) {
        super(materialIn);
        shipSpawnDetectorID = DetectorManager.DetectorIDs.ShipSpawnerGeneral.ordinal();
    }

    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityPhysicsInfuser();
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (!worldIn.isRemote) {
            BlockPos dummyPos = getDummyStatePos(state, pos);
            worldIn.setBlockState(dummyPos, ValkyrienWarfareMod.INSTANCE.physicsInfuserDummy.getDefaultState()
                    .withProperty(FACING, getDummyStateFacing(state)));
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntityPhysicsInfuser te = (TileEntityPhysicsInfuser) worldIn.getTileEntity(pos);
        IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        // Drop all the items
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack);
        }
        BlockPos dummyPos = getDummyStatePos(state, pos);
        super.breakBlock(worldIn, pos, state);
        if (worldIn.getBlockState(dummyPos)
                .getBlock() == ValkyrienWarfareMod.INSTANCE.physicsInfuserDummy) {
            worldIn.setBlockToAir(dummyPos);
        }
    }

    public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) {
        itemInformation.add(TextFormatting.BLUE
                + "Turns any blocks attatched to this one into a brand new Ship, just be careful not to infuse your entire world");
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        EnumFacing facingHorizontal = placer.getHorizontalFacing();

        if (!placer.isSneaking()) {
            facingHorizontal = facingHorizontal.getOpposite();
        }

        return this.getDefaultState()
                .withProperty(FACING, facingHorizontal);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            playerIn.openGui(ValkyrienWarfareMod.INSTANCE, VW_Gui_Enum.PHYSICS_INFUSER.ordinal(), worldIn, pos.getX(), pos.getY(), pos.getZ());

            /*
            WorldPhysObjectManager manager = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getManagerForWorld(worldIn);
            if (manager != null) {
                PhysicsWrapperEntity wrapperEnt = manager
                        .getManagingObjectForChunk(worldIn.getChunkFromBlockCoords(pos));
                if (wrapperEnt != null) {
                    wrapperEnt.getPhysicsObject().setPhysicsEnabled(!wrapperEnt.getPhysicsObject().isPhysicsEnabled());
                    // Alright (try) to destroy this ship.
                    wrapperEnt.getPhysicsObject()
                            .tryToDeconstructShip();
                    return true;
                }
            }

            if (ValkyrienWarfareMod.canChangeAirshipCounter(true, playerIn)) {
                PhysicsWrapperEntity wrapper = new PhysicsWrapperEntity(worldIn, pos.getX(), pos.getY(), pos.getZ(),
                        playerIn, shipSpawnDetectorID, ShipType.Full_Unlocked);
                worldIn.spawnEntity(wrapper);
            } else {
                playerIn.sendMessage(new TextComponentString(
                        "You've made too many airships! The limit per player is " + ValkyrienWarfareMod.maxAirships));
            }

             */
        }
        return true;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        if (super.canPlaceBlockAt(worldIn, pos)) {
            return worldIn.getBlockState(pos.north())
                    .getBlock()
                    .isReplaceable(worldIn, pos) && worldIn.getBlockState(pos.south())
                    .getBlock()
                    .isReplaceable(worldIn, pos) && worldIn.getBlockState(pos.east())
                    .getBlock()
                    .isReplaceable(worldIn, pos) && worldIn.getBlockState(pos.west())
                    .getBlock()
                    .isReplaceable(worldIn, pos);
        } else {
            return false;
        }
    }

    private BlockPos getDummyStatePos(IBlockState state, BlockPos pos) {
        EnumFacing dummyBlockFacing = state.getValue(FACING)
                .rotateY()
                .getOpposite();
        return pos.offset(dummyBlockFacing);
    }

    private EnumFacing getDummyStateFacing(IBlockState state) {
        return state.getValue(FACING).rotateY();
    }
}