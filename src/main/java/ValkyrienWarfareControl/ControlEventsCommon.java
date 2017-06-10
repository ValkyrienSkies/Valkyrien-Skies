package ValkyrienWarfareControl;

import ValkyrienWarfareControl.Capability.ICapabilityLastRelay;
import ValkyrienWarfareControl.Item.ItemRelayWire;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ControlEventsCommon {

	@SubscribeEvent
	public void onAttachCapabilityEventItem(AttachCapabilitiesEvent event){
		if(event instanceof AttachCapabilitiesEvent.Item){
			AttachCapabilitiesEvent.Item itemEvent = (AttachCapabilitiesEvent.Item)event;
			ItemStack stack = itemEvent.getItemStack();

			if(itemEvent.getItem() instanceof ItemRelayWire){

//				System.out.println("Obama?");

				event.addCapability(new ResourceLocation(ValkyrienWarfareControlMod.MODID, "LastRelay"), new ICapabilitySerializable<NBTTagIntArray>() {
					ICapabilityLastRelay inst = ValkyrienWarfareControlMod.lastRelayCapability.getDefaultInstance();

					@Override
					public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
						return capability == ValkyrienWarfareControlMod.lastRelayCapability;
					}

					@Override
					public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
						return capability == ValkyrienWarfareControlMod.lastRelayCapability ? ValkyrienWarfareControlMod.lastRelayCapability.<T>cast(inst) : null;
					}

					@Override
					public NBTTagIntArray serializeNBT() {
						return (NBTTagIntArray) ValkyrienWarfareControlMod.lastRelayCapability.getStorage().writeNBT(ValkyrienWarfareControlMod.lastRelayCapability, inst, null);
					}

					@Override
					public void deserializeNBT(NBTTagIntArray nbt) {
						ValkyrienWarfareControlMod.lastRelayCapability.getStorage().readNBT(ValkyrienWarfareControlMod.lastRelayCapability, inst, null, nbt);
					}
				});
			}
		}
	}

}
