package org.valkyrienskies.mod.common.capability;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.capability.entity_backup.ICapabilityEntityBackup;
import org.valkyrienskies.mod.common.capability.entity_backup.ImplCapabilityEntityBackup;
import org.valkyrienskies.mod.common.capability.framework.VSDefaultCapabilityProvider;
import org.valkyrienskies.mod.common.capability.framework.VSDefaultCapabilityProviderTransient;
import org.valkyrienskies.mod.common.capability.framework.VSDefaultCapabilityStorage;
import org.valkyrienskies.mod.common.capability.framework.VSDefaultCapabilityTransientStorage;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

@EventBusSubscriber(modid = ValkyrienSkiesMod.MOD_ID)
public class VSCapabilityRegistry {

    @CapabilityInject(VSWorldDataCapability.class)
    public static final Capability<VSWorldDataCapability> VS_WORLD_DATA = ValkyrienUtils.getFakeNull();

    @CapabilityInject(ICapabilityEntityBackup.class)
    public static final Capability<ICapabilityEntityBackup> VS_ENTITY_BACKUP = ValkyrienUtils.getFakeNull();

    @SubscribeEvent
    public static void attachWorldCapabilities(AttachCapabilitiesEvent<World> event) {
        event.addCapability(
            new ResourceLocation(ValkyrienSkiesMod.MOD_ID, "world_data_capability"),
            new VSDefaultCapabilityProvider<>(VS_WORLD_DATA));
    }

    @SubscribeEvent
    public static void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        event.addCapability(
            new ResourceLocation(ValkyrienSkiesMod.MOD_ID, "entity_backup_capability"),
            new VSDefaultCapabilityProviderTransient<>(VS_ENTITY_BACKUP));
    }

    public static void registerCapabilities() {
        CapabilityManager.INSTANCE.register(
            VSWorldDataCapability.class,
            new VSDefaultCapabilityStorage<>(),
            VSWorldDataCapability::new
        );

        CapabilityManager.INSTANCE.register(
            ICapabilityEntityBackup.class,
            new VSDefaultCapabilityTransientStorage<>(),
            ImplCapabilityEntityBackup::new
        );
    }

}
