package valkyrienwarfare.addon.control;

import java.lang.annotation.ElementType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import valkyrienwarfare.addon.control.piloting.IShipPilot;
import valkyrienwarfare.addon.control.tileentity.ImplTileEntityPilotable;

public class ControlEventsClient {
	
	@SubscribeEvent
	public void render(RenderGameOverlayEvent.Post event) {
		Minecraft minecraft = Minecraft.getMinecraft();
		EntityPlayer player = Minecraft.getMinecraft().player;
		FontRenderer fontRenderer = minecraft.fontRenderer;
		if (fontRenderer != null && player != null && event.getType() == net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.TEXT) {
			IShipPilot playerPilot = IShipPilot.class.cast(player);
			if (playerPilot.isPiloting()) {
				BlockPos tilePilotedPos = playerPilot.getPosBeingControlled();
				TileEntity pilotedTile = player.getEntityWorld().getTileEntity(tilePilotedPos);
				if (pilotedTile != null && pilotedTile instanceof ImplTileEntityPilotable) {
					ImplTileEntityPilotable pilotedControlEntity = (ImplTileEntityPilotable) pilotedTile;
					ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
					pilotedControlEntity.renderPilotText(fontRenderer, scaledresolution);
				}
			}
		}
	}
}
