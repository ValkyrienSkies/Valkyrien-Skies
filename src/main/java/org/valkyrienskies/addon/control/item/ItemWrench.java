package org.valkyrienskies.addon.control.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.valkyrienskies.addon.control.MultiblockRegistry;
import org.valkyrienskies.addon.control.block.multiblocks.GiantPropellerMultiblockSchematic;
import org.valkyrienskies.addon.control.block.multiblocks.IMulitblockSchematic;
import org.valkyrienskies.addon.control.block.multiblocks.RudderAxleMultiblockSchematic;
import org.valkyrienskies.addon.control.block.multiblocks.TileEntityEthereumCompressorPart;
import org.valkyrienskies.addon.control.block.multiblocks.TileEntityEthereumEnginePart;
import org.valkyrienskies.addon.control.block.multiblocks.TileEntityGiantPropellerPart;
import org.valkyrienskies.addon.control.block.multiblocks.TileEntityRudderAxlePart;
import org.valkyrienskies.addon.control.tileentity.TileEntityGearbox;

public class ItemWrench extends Item {

    public ItemWrench() {
        this.setMaxStackSize(1);
        this.setMaxDamage(80);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player,
        List<String> itemInformation,
        ITooltipFlag advanced) {
        itemInformation.add(TextFormatting.BLUE + I18n.format("tooltip.vs_control.wrench"));
    }


    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos,
        EnumHand hand,
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
            ((TileEntityGearbox) blockTile)
                .setInputFacing(!player.isSneaking() ? facing : facing.getOpposite());
        }

        return EnumActionResult.PASS;
    }
}
