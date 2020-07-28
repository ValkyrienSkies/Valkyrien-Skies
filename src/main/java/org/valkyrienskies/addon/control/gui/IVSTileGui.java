package org.valkyrienskies.addon.control.gui;

import net.minecraft.entity.player.EntityPlayer;

/**
 * A simple interface that allows multiple types of tile entities to use the same button press
 * packet.
 */
public interface IVSTileGui {

    /**
     * Called on both client and server side when a player presses the button with the given
     * buttonId.
     */
    void onButtonPress(int buttonId, EntityPlayer presser);
}
