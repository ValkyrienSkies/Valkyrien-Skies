package valkyrienwarfare.mod.client.gui;

import net.minecraft.entity.player.EntityPlayer;

/**
 * A simple interface that allows multiple types of tile entities to use the same button press packet.
 */
public interface IVWTileGui {

    /**
     * Called on both client and server side when a player presses the button with the given buttonId.
     *
     * @param buttonId
     * @param presser
     */
    void onButtonPress(int buttonId, EntityPlayer presser);
}
