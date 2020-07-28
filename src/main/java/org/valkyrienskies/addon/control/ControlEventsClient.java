package org.valkyrienskies.addon.control;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.valkyrienskies.addon.control.renderer.infuser_core_rendering.InfuserCoreBakedModel;
import org.valkyrienskies.mod.common.piloting.IShipPilot;
import org.valkyrienskies.mod.common.piloting.ITileEntityPilotable;

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

    /**
     * Force the game to load the inventory texture for physics core.
     */
    @SubscribeEvent
    public void onTextureStitchPre(TextureStitchEvent.Pre event) {
        ResourceLocation mainCoreInventoryTexture = new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "items/main_core");
        ResourceLocation smallCoreInventoryTexture = new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "items/small_core");
        event.getMap()
                .registerSprite(mainCoreInventoryTexture);
        event.getMap()
                .registerSprite(smallCoreInventoryTexture);
    }

    /**
     * Replace the item model of the physics core with the custom behavior one.
     */
    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        ResourceLocation modelResourceLocation = new ResourceLocation(ValkyrienSkiesControl.MOD_ID,
                "item/infuser_core_main");
        try {
            IModel model = ModelLoaderRegistry.getModel(modelResourceLocation);
            IBakedModel inventoryModel = model
                    .bake(model.getDefaultState(), DefaultVertexFormats.ITEM,
                            ModelLoader.defaultTextureGetter());
            IBakedModel handModel = event.getModelRegistry()
                    .getObject(new ModelResourceLocation(
                            ValkyrienSkiesControl.MOD_ID + ":" + ValkyrienSkiesControl.INSTANCE.physicsCore
                                    .getTranslationKey()
                                    .substring(5), "inventory"));

            event.getModelRegistry()
                    .putObject(
                            new ModelResourceLocation(ValkyrienSkiesControl.MOD_ID + ":testmodel", "inventory"),
                            new InfuserCoreBakedModel(handModel, inventoryModel));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
