package org.valkyrienskies.mod.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.ships.ShipData;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransform;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.util.BaseItem;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

import java.text.DecimalFormat;
import java.util.Optional;
import java.util.UUID;

public class ItemShipTracker extends BaseItem {

    private static final String NBT_DATA_KEY = "vs_tracked_ship_uuid";

    public ItemShipTracker(String name, boolean creativeTab) {
        super(name, creativeTab);
        this.maxStackSize = 1;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            final ItemStack heldItemStack = player.getHeldItem(hand);
            final NBTTagCompound stackTagCompound;
            if (heldItemStack.hasTagCompound()) {
                stackTagCompound = heldItemStack.stackTagCompound;
            } else {
                stackTagCompound = new NBTTagCompound();
            }

            final Optional<PhysicsObject> shipObjectOptional = ValkyrienUtils.getPhysoManagingBlock(worldIn, pos);
            if (shipObjectOptional.isPresent()) {
                final PhysicsObject shipObject = shipObjectOptional.get();
                stackTagCompound.setString(NBT_DATA_KEY, shipObject.getUuid().toString());
                heldItemStack.setTagCompound(stackTagCompound);
                heldItemStack.setStackDisplayName(shipObject.getName() + " tracker");
                player.sendMessage(new TextComponentString(String.format("Now tracking ship %s.", shipObject.getName())));
                return EnumActionResult.SUCCESS;
            }
        }

        return EnumActionResult.PASS;
    }

    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand hand) {
        if (!worldIn.isRemote) {
            final ItemStack heldItemStack = player.getHeldItem(hand);
            final NBTTagCompound stackTagCompound;
            if (heldItemStack.hasTagCompound()) {
                stackTagCompound = heldItemStack.stackTagCompound;
            } else {
                stackTagCompound = new NBTTagCompound();
            }

            if (stackTagCompound.hasKey(NBT_DATA_KEY)) {
                // Tell the player the ship location
                final UUID shipUUID = UUID.fromString(stackTagCompound.getString(NBT_DATA_KEY));
                final Optional<ShipData> shipDataOptional = ValkyrienUtils.getQueryableData(worldIn).getShip(shipUUID);

                if (shipDataOptional.isPresent()) {
                    final ShipData shipData = shipDataOptional.get();
                    final ShipTransform currentTransform = shipData.getShipTransform();
                    final Vector3d shipPosition = new Vector3d(currentTransform.getPosX(), currentTransform.getPosY(), currentTransform.getPosZ());

                    // Only print up to 2 digits after the decimal place
                    final String shipPositionString = shipPosition.toString(new DecimalFormat("############.##"));

                    player.sendMessage(new TextComponentString(String.format("The ship %s is currently at %s.", shipData.getName(), shipPositionString)));
                } else {
                    player.sendMessage(new TextComponentString(String.format("No ship with UUID %s found! Maybe it was destroyed.", shipUUID.toString())));
                }
            } else {
                player.sendMessage(new TextComponentString("Not tracking any ships. Right click on a ship to track it."));
            }
        }
        return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
    }
}
