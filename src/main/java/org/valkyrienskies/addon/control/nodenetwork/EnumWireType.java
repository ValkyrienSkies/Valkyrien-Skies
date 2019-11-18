package org.valkyrienskies.addon.control.nodenetwork;

import net.minecraft.item.Item;
import org.valkyrienskies.addon.control.ValkyrienSkiesControl;

public enum EnumWireType {
    RELAY("relay_wire", ValkyrienSkiesControl.INSTANCE.relayWire), VANISHING("vanishing_wire", ValkyrienSkiesControl.INSTANCE.relayWire);

    private String name;
    private Item item;

    EnumWireType(String name, Item item) {
        this.name = name;
        this.item = item;
    }

    public String toString() {
        return this.name;
    }

    public Item toItem() {
        return this.item;
    }
}
