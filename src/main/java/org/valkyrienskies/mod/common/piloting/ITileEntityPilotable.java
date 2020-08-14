package org.valkyrienskies.mod.common.piloting;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;

public interface ITileEntityPilotable {

    void onPilotControlsMessage(PilotControlsMessage message, EntityPlayerMP sender);

    EntityPlayer getPilotEntity();

    void setPilotEntity(EntityPlayer newPilot);

    void playerWantsToStopPiloting(EntityPlayer player);

    PhysicsObject getParentPhysicsEntity();

    default void onStartTileUsage() {
    }

    default void onStopTileUsage() {
    }

    /**
     * This is called during the post render of every frame in Minecraft. Override this to allow a
     * pilot tileentity to display info as text on the screen.
     *
     * @param renderer
     * @param gameResolution
     */
    @SideOnly(Side.CLIENT)
    default void renderPilotText(FontRenderer renderer, ScaledResolution gameResolution) {

    }
}
