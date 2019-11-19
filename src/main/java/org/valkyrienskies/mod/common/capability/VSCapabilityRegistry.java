package org.valkyrienskies.mod.common.capability;

import javax.annotation.Nonnull;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
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

@EventBusSubscriber(modid = ValkyrienSkiesMod.MOD_ID)
public class VSCapabilityRegistry {

    @CapabilityInject(VSWorldDataCapability.class)
    public static final Capability<VSWorldDataCapability> VS_WORLD_DATA = getNull();

    @CapabilityInject(ICapabilityEntityBackup.class)
    public static final Capability<ICapabilityEntityBackup> VS_ENTITY_BACKUP = getNull();

    @CapabilityInject(VSChunkPhysoCapability.class)
    public static final Capability<VSChunkPhysoCapability> VS_CHUNK_PHYSO = getNull();

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

    @SubscribeEvent
    public static void attachChunkCapabilities(AttachCapabilitiesEvent<Chunk> event) {
        event.addCapability(
            new ResourceLocation(ValkyrienSkiesMod.MOD_ID, "chunk_physo_capability"),
            new VSDefaultCapabilityProviderTransient<>(VS_CHUNK_PHYSO));
    }

    public static void registerCapabilities() {
        CapabilityManager.INSTANCE.register(
            VSWorldDataCapability.class,
            new VSDefaultCapabilityStorage<>(),
            VSWorldDataCapability::new
        );

        CapabilityManager.INSTANCE.register(
            VSChunkPhysoCapability.class,
            new VSDefaultCapabilityTransientStorage<>(),
            VSChunkPhysoCapability::new
        );

        CapabilityManager.INSTANCE.register(
            ICapabilityEntityBackup.class,
            new VSDefaultCapabilityTransientStorage<>(),
            ImplCapabilityEntityBackup::new
        );
    }


    /**
     * Used to trick the IDE into thinking that a capability is not null
     *
     * @return null
     * @see <a href="https://stackoverflow.com/questions/46512161/disable-constant-conditions-except
     * ions-inspection-for-field-in-intellij-idea">StackOverflow</a>
     */
    @Nonnull
    @SuppressWarnings({"ConstantConditions", "SameReturnValue"})
    public static <T> T getNull() {
        return null;
    }

}
