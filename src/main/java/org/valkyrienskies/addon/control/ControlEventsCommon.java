package org.valkyrienskies.addon.control;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.valkyrienskies.addon.control.capability.LastNodeCapabilityProvider;
import org.valkyrienskies.addon.control.item.ItemBaseWire;

public class ControlEventsCommon {

    @SubscribeEvent
    public void onAttachCapabilityEventItem(AttachCapabilitiesEvent<ItemStack> event) {
        if (event.getObject().getItem() instanceof ItemBaseWire) {
            event.addCapability(new ResourceLocation(ValkyrienSkiesControl.MOD_ID, "LastRelay"),
                new LastNodeCapabilityProvider());
        }
    }
}
