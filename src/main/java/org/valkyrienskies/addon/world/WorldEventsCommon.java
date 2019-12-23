package org.valkyrienskies.addon.world;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import org.valkyrienskies.addon.world.block.BlockValkyriumOre;
import org.valkyrienskies.addon.world.capability.AntiGravityCapabilityProvider;
import org.valkyrienskies.addon.world.capability.ICapabilityAntiGravity;
import org.valkyrienskies.mod.common.config.VSConfig;

public class WorldEventsCommon {

    @SubscribeEvent
    public void onAttachCapabilityEventItem(AttachCapabilitiesEvent event) {
        if (event.getObject() instanceof ItemStack) {
            ItemStack stack = (ItemStack) event.getObject();
            Item item = stack.getItem();

            if (item instanceof ItemValkyriumCrystal) {
                event.addCapability(
                    new ResourceLocation(ValkyrienSkiesWorld.MOD_ID, "AntiGravityValue"),
                    new AntiGravityCapabilityProvider(VSConfig.valkyriumCrystalForce));
            }
            if (stack.getItem() instanceof ItemBlock) {
                ItemBlock blockItem = (ItemBlock) stack.getItem();
                if (blockItem.getBlock() instanceof BlockValkyriumOre) {
                    event.addCapability(
                        new ResourceLocation(ValkyrienSkiesWorld.MOD_ID, "AntiGravityValue"),
                        new AntiGravityCapabilityProvider(VSConfig.valkyriumOreForce));
                }
            }
        }
    }

    @SubscribeEvent
    public void worldTick(WorldTickEvent event) {
        if (event.phase == Phase.START) {
            for (Entity entity : event.world.loadedEntityList) {
                if (entity instanceof EntityItem) {
                    EntityItem itemEntity = (EntityItem) entity;
                    ItemStack itemStack = itemEntity.getItem();
                    ICapabilityAntiGravity capability = itemStack.getCapability(ValkyrienSkiesWorld.ANTI_GRAVITY_CAPABILITY, null);
                    if (capability != null) {
                        if (capability.getMultiplier() != 0) {
                            double multiplier = 0.12 / capability.getMultiplier(); // trust me it multiplies Y increase
                            itemEntity.addVelocity(0, .1 - (itemEntity.motionY * multiplier), 0);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void playerTick(PlayerTickEvent event) {
        if (event.phase == Phase.START) {
            EntityPlayer player = event.player;
            //TODO: fix the fall damage
            // @thebest108: what fall damage?
            //                    --DaPorkchop_, 28/03/2017
            if (VSConfig.doValkyriumLifting && !player.isCreative()) {
                for (NonNullList<ItemStack> stackArray : player.inventory.allInventories) {
                    for (ItemStack stack : stackArray) {
                        if (stack != null) {
                            if (stack.getItem() instanceof ItemBlock) {
                                ItemBlock blockItem = (ItemBlock) stack.getItem();
                                if (blockItem.getBlock() instanceof BlockValkyriumOre) {
                                    player.addVelocity(0, .0025D * stack.stackSize * VSConfig.valkyriumOreForce, 0);
                                }
                            } else if (stack.getItem() instanceof ItemValkyriumCrystal) {
                                player.addVelocity(0, .0025D * stack.stackSize * VSConfig.valkyriumCrystalForce, 0);
                            }
                        }
                    }
                }
            }
        }
    }
}
