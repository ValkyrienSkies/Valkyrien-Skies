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

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.mod.physmanagement.relocation.DetectorManager;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;
import valkyrienwarfare.physics.management.ShipType;
import valkyrienwarfare.physics.management.WorldPhysObjectManager;

import java.util.List;

public class BlockPhysicsInfuser extends Block {

    int shipSpawnDetectorID;

    public BlockPhysicsInfuser(Material materialIn) {
        super(materialIn);
        shipSpawnDetectorID = DetectorManager.DetectorIDs.ShipSpawnerGeneral.ordinal();
    }

    public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) {
        itemInformation.add(TextFormatting.BLUE
                + "Turns any blocks attatched to this one into a brand new Ship, just be careful not to infuse your entire world");
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            // ===== Debug Testing Code =====
            /*
             * System.out.println("yaw: " + playerIn.rotationYaw + "    pitch: " +
             * playerIn.rotationPitch); VectorImmutable immutable = (new
             * Vector(playerIn.getLookVec())).toImmutable(); double pitch =
             * VWMath.getPitchFromVectorImmutable(immutable); double yaw =
             * VWMath.getYawFromVectorImmutable(immutable, pitch);
             * System.out.println("yaw2: " + yaw + "    pitch2: " + pitch);
             * System.out.println("Position: " + playerIn.getPositionVector()); if (true) {
             * return false; }
             */
            // ===== Debug Code End =====
            WorldPhysObjectManager manager = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getManagerForWorld(worldIn);
            if (manager != null) {
                PhysicsWrapperEntity wrapperEnt = manager
                        .getManagingObjectForChunk(worldIn.getChunkFromBlockCoords(pos));
                if (wrapperEnt != null) {
                    wrapperEnt.getPhysicsObject().setPhysicsEnabled(!wrapperEnt.getPhysicsObject().isPhysicsEnabled());
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

}