package valkyrienwarfare.mod.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.mod.container.ContainerPhysicsInfuser;
import valkyrienwarfare.mod.tileentity.TileEntityPhysicsInfuser;

public class GuiPhysicsInfuser extends GuiContainer {

    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(ValkyrienWarfareMod.MODID, "textures/gui/container/physicsinfuserguinocoreson.png");

    public GuiPhysicsInfuser(EntityPlayer player, TileEntityPhysicsInfuser te) {
        super(new ContainerPhysicsInfuser(player, te));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager()
                .bindTexture(BACKGROUND_TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
    }
}
