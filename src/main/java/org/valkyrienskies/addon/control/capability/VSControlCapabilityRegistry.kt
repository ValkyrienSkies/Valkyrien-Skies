package org.valkyrienskies.addon.control.capability

import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.valkyrienskies.addon.control.ValkyrienSkiesControl
import org.valkyrienskies.addon.control.nodegraph.VSControlData
import org.valkyrienskies.addon.control.nodegraph.VSControlDataCapability
import org.valkyrienskies.mod.common.ValkyrienSkiesMod
import org.valkyrienskies.mod.common.capability.VSCapabilityRegistry
import org.valkyrienskies.mod.common.capability.framework.VSDefaultCapabilityProvider
import org.valkyrienskies.mod.common.capability.framework.VSDefaultCapabilityStorage
import org.valkyrienskies.mod.common.util.ValkyrienUtils

object VSControlCapabilityRegistry {

    @CapabilityInject(VSControlDataCapability::class)
    val CONTROL_DATA: Capability<VSControlDataCapability> = ValkyrienUtils.getNull()

    fun getControlData(world: World): VSControlData =
            world.getCapability(CONTROL_DATA, null)!!.get()

    @SubscribeEvent
    fun attachWorldCapabilities(event: AttachCapabilitiesEvent<World?>) {
        event.addCapability(
                ResourceLocation(ValkyrienSkiesControl.MOD_ID, "world_data_capability"),
                VSDefaultCapabilityProvider(CONTROL_DATA))
    }

    fun registerCapabilities() {
        CapabilityManager.INSTANCE.register(
                VSControlDataCapability::class.java,
                VSDefaultCapabilityStorage(),
                { VSControlDataCapability() }
        )
    }
}