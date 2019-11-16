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
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.valkyrienskies.addon.control.MultiblockRegistry;
import org.valkyrienskies.addon.control.block.multiblocks.GiantPropellerMultiblockSchematic;
import org.valkyrienskies.addon.control.block.multiblocks.IMultiblockSchematic;
import org.valkyrienskies.addon.control.block.multiblocks.ITileEntityMultiblockPart;
import org.valkyrienskies.addon.control.block.multiblocks.RudderAxleMultiblockSchematic;
import org.valkyrienskies.addon.control.block.multiblocks.TileEntityGiantPropellerPart;
import org.valkyrienskies.addon.control.block.multiblocks.TileEntityRudderPart;
import org.valkyrienskies.addon.control.block.multiblocks.TileEntityValkyriumCompressorPart;
import org.valkyrienskies.addon.control.block.multiblocks.TileEntityValkyriumEnginePart;
import org.valkyrienskies.addon.control.tileentity.TileEntityGearbox;

public class ItemWrench extends Item {
    private EnumWrenchMode mode = EnumWrenchMode.CONSTRUCT;

    public ItemWrench() {
        this.setMaxStackSize(1);
        this.setMaxDamage(80);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player,
        List<String> itemInformation,
        ITooltipFlag advanced) {
        itemInformation.add(TextFormatting.BLUE + I18n.format("tooltip.vs_control.wrench." + this.mode.toString()));
        itemInformation.add(TextFormatting.GREEN + "" + TextFormatting.ITALIC + I18n.format("tooltip.vs_control.wrench_modes"));
    }

    // Construct potential multiblock if set to construct mode.
    // Otherwise, try to deconstruct a multiblock.
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos,
        EnumHand hand,
        EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) {
            return EnumActionResult.SUCCESS;
        }

        if (player.isSneaking()) {
            this.mode = EnumWrenchMode.values()[(this.mode.ordinal() + 1) % EnumWrenchMode.values().length]; // Switch to the next mode
            player.sendMessage(new TextComponentString(
                TextFormatting.BLUE + I18n.format("tooltip.vs_control.wrench_switched", this.mode.toString()))); // Say in chat
            return EnumActionResult.SUCCESS;
        }

        IBlockState clickedState = worldIn.getBlockState(pos);
        TileEntity blockTile = worldIn.getTileEntity(pos);
        if (this.mode == EnumWrenchMode.CONSTRUCT) {
            if (blockTile instanceof TileEntityValkyriumEnginePart) {
                List<IMultiblockSchematic> valkyriumEngineMultiblockSchematics = MultiblockRegistry
                    .getSchematicsWithPrefix("multiblock_valkyrium_engine");
                for (IMultiblockSchematic schematic : valkyriumEngineMultiblockSchematics) {
                    if (schematic.attemptToCreateMultiblock(worldIn, pos)) {
                        return EnumActionResult.SUCCESS;
                    }
                }
            }

            if (blockTile instanceof TileEntityValkyriumCompressorPart) {
                List<IMultiblockSchematic> valkyriumEngineMultiblockSchematics = MultiblockRegistry
                    .getSchematicsWithPrefix("multiblock_valkyrium_compressor");
                for (IMultiblockSchematic schematic : valkyriumEngineMultiblockSchematics) {
                    if (schematic.attemptToCreateMultiblock(worldIn, pos)) {
                        return EnumActionResult.SUCCESS;
                    }
                }
            }

            if (blockTile instanceof TileEntityRudderPart) {
                List<IMultiblockSchematic> rudderAxleMultiblockSchematics = MultiblockRegistry
                    .getSchematicsWithPrefix("multiblock_rudder_axle");
                for (IMultiblockSchematic schematic : rudderAxleMultiblockSchematics) {
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
                List<IMultiblockSchematic> giantPropellerMultiblockSchematics = MultiblockRegistry
                    .getSchematicsWithPrefix("multiblock_giant_propeller");
                for (IMultiblockSchematic schematic : giantPropellerMultiblockSchematics) {
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
        } else if (this.mode == EnumWrenchMode.DECONSTRUCT && blockTile instanceof ITileEntityMultiblockPart) {
            ((ITileEntityMultiblockPart) blockTile).disassembleMultiblock();
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    public EnumWrenchMode getMode() {
        return this.mode;
    }
}
