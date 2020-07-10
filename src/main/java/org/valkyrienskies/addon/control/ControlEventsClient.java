package org.valkyrienskies.addon.control;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.valkyrienskies.mod.common.piloting.IShipPilot;
import org.valkyrienskies.mod.common.piloting.ITileEntityPilotable;
import org.valkyrienskies.mod.common.tileentity.TileEntityPilotableImpl;

public class ControlEventsClient {

    @SubscribeEvent
    public void render(RenderGameOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayer player = Minecraft.getMinecraft().player;
        FontRenderer fontRenderer = minecraft.fontRenderer;
        if (fontRenderer != null && player != null && event.getType()
            == net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.TEXT) {
            IShipPilot playerPilot = (IShipPilot) player;
            if (playerPilot.isPiloting()) {
                BlockPos tilePilotedPos = playerPilot.getPosBeingControlled();
                TileEntity pilotedTile = player.getEntityWorld().getTileEntity(tilePilotedPos);
                if (pilotedTile instanceof ITileEntityPilotable) {
                    ITileEntityPilotable pilotedControlEntity = (ITileEntityPilotable) pilotedTile;
                    ScaledResolution scaledresolution = new ScaledResolution(
                        Minecraft.getMinecraft());
                    pilotedControlEntity.renderPilotText(fontRenderer, scaledresolution);
                }
            }
        }
    }

}
