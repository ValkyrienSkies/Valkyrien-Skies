package org.valkyrienskies.addon.control.item;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.valkyrienskies.addon.control.MultiblockRegistry;
import org.valkyrienskies.addon.control.block.multiblocks.*;
import org.valkyrienskies.addon.control.tileentity.TileEntityGearbox;

import java.util.List;

public class ItemWrench extends Item {

    public ItemWrench() {
        this.setMaxStackSize(1);
        this.setMaxDamage(80);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) {
            return EnumActionResult.SUCCESS;
        }
        IBlockState clickedState = worldIn.getBlockState(pos);
        TileEntity blockTile = worldIn.getTileEntity(pos);

        if (blockTile instanceof TileEntityEthereumEnginePart) {
            List<IMulitblockSchematic> ethereumEngineMultiblockSchematics = MultiblockRegistry
                    .getSchematicsWithPrefix("multiblock_ether_engine");
            for (IMulitblockSchematic schematic : ethereumEngineMultiblockSchematics) {
                if (schematic.attemptToCreateMultiblock(worldIn, pos)) {
                    return EnumActionResult.SUCCESS;
                }
            }
        }

        if (blockTile instanceof TileEntityEthereumCompressorPart) {
            List<IMulitblockSchematic> ethereumEngineMultiblockSchematics = MultiblockRegistry
                    .getSchematicsWithPrefix("multiblock_ether_compressor");
            for (IMulitblockSchematic schematic : ethereumEngineMultiblockSchematics) {
                if (schematic.attemptToCreateMultiblock(worldIn, pos)) {
                    return EnumActionResult.SUCCESS;
                }
            }
        }

        if (blockTile instanceof TileEntityRudderAxlePart) {
            List<IMulitblockSchematic> rudderAxelMultiblockSchematics = MultiblockRegistry
                    .getSchematicsWithPrefix("multiblock_rudder_axle");
            for (IMulitblockSchematic schematic : rudderAxelMultiblockSchematics) {
                RudderAxleMultiblockSchematic rudderSchem = (RudderAxleMultiblockSchematic) schematic;
                if (facing.getAxis() != rudderSchem.getAxleAxisDirection().getAxis()) {
                    if (rudderSchem.getAxleFacingDirection() == facing) {
                        if (schematic.attemptToCreateMultiblock(worldIn, pos)) {
                            return EnumActionResult.SUCCESS;
                        }
                    }

                }
            }
        }

        if (blockTile instanceof TileEntityGiantPropellerPart) {
            List<IMulitblockSchematic> giantPropellerMultiblockSchematics = MultiblockRegistry
                    .getSchematicsWithPrefix("multiblock_giant_propeller");
            for (IMulitblockSchematic schematic : giantPropellerMultiblockSchematics) {
                GiantPropellerMultiblockSchematic propSchem = (GiantPropellerMultiblockSchematic) schematic;
                if (propSchem.getPropellerFacing() == facing) {
                    if (schematic.attemptToCreateMultiblock(worldIn, pos)) {
                        return EnumActionResult.SUCCESS;
                    }
                }
            }
        }

        if (blockTile instanceof TileEntityGearbox) {
            ((TileEntityGearbox) blockTile).setInputFacing(!player.isSneaking() ? facing : facing.getOpposite());
        }

        return EnumActionResult.PASS;
    }
}
