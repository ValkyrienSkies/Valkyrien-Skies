package org.valkyrienskies.addon.control.nodenetwork

import net.minecraft.item.Item
import org.valkyrienskies.addon.control.ValkyrienSkiesControl


enum class WireType(
        val wireName: String,
        val item: Item?
) {
    RELAY("relay_wire", ValkyrienSkiesControl.INSTANCE.relayWire),
    VANISHING("vanishing_wire", ValkyrienSkiesControl.INSTANCE.vanishingWire);
}