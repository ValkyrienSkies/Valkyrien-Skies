package org.valkyrienskies.mod.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.network.MessageStartPiloting;
import org.valkyrienskies.mod.common.piloting.ControllerInputType;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.ships.ship_world.ShipPilot;
import org.valkyrienskies.mod.common.util.BaseItem;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

import java.util.Optional;

public class ItemShipOar extends BaseItem {

    public ItemShipOar(String name, boolean creativeTab) {
        super(name, creativeTab);
        this.maxStackSize = 1;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote && player != null) {
            final Optional<PhysicsObject> shipObjectOptional = ValkyrienUtils.getPhysoManagingBlock(worldIn, pos);
            if (shipObjectOptional.isPresent()) {
                // Let the player pilot the ship
                final PhysicsObject shipObject = shipObjectOptional.get();

                shipObject.setShipPilot(new ShipPilot(player));

                final MessageStartPiloting startMessage = new MessageStartPiloting(shipObject.getUuid(), ControllerInputType.CaptainsChair);
                ValkyrienSkiesMod.controlNetwork.sendTo(startMessage, (EntityPlayerMP) player);

                player.sendMessage(new TextComponentString(String.format("Now piloting ship %s.", shipObject.getName())));
                return EnumActionResult.SUCCESS;
            }
        }

        return EnumActionResult.PASS;
    }

}
