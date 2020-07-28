package org.valkyrienskies.addon.control.gui;

import static org.valkyrienskies.addon.control.container.EnumInfuserButton.ALIGN_SHIP;
import static org.valkyrienskies.addon.control.container.EnumInfuserButton.ASSEMBLE_SHIP;
import static org.valkyrienskies.addon.control.container.EnumInfuserButton.ENABLE_PHYSICS;
import static org.valkyrienskies.addon.control.container.EnumInfuserButton.values;

import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.addon.control.container.ContainerPhysicsInfuser;
import org.valkyrienskies.addon.control.container.EnumInfuserButton;
import org.valkyrienskies.addon.control.tileentity.TileEntityPhysicsInfuser;

public class GuiPhysicsInfuser extends GuiContainer {

    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(
        ValkyrienSkiesMod.MOD_ID, "textures/gui/container/physics_infuser_gui.png");
    private final TileEntityPhysicsInfuser tileEntity;

    public GuiPhysicsInfuser(EntityPlayer player, TileEntityPhysicsInfuser tileEntity) {
        super(new ContainerPhysicsInfuser(player, tileEntity));
        this.tileEntity = tileEntity;
    }

    @Override
    public void initGui() {
        super.initGui();
        super.buttonList.clear();

        GuiButton buttonAssembleShip = new GuiButton(ASSEMBLE_SHIP.ordinal(), (width / 2) + 90,
            (height / 2) - 70,
            98, 20, "");
        GuiButton buttonEnablePhysics = new GuiButton(ENABLE_PHYSICS.ordinal(), (width / 2) + 90,
            (height / 2) - 45,
            98, 20, "");
        GuiButton buttonAlignShip = new GuiButton(ALIGN_SHIP.ordinal(), (width / 2) + 90,
            (height / 2) - 20,
            98, 20, "");

        updateButtonStatus();

        super.buttonList.add(buttonAssembleShip);
        super.buttonList.add(buttonEnablePhysics);
        super.buttonList.add(buttonAlignShip);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        updateButtonStatus();
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

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        this.fontRenderer.drawString(I18n.format("gui.physics_infuser"), 4, 4, 0x00404040);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        this.tileEntity.onButtonPress(button.id, Minecraft.getMinecraft().player);
    }

    private void updateButtonStatus() {
        for (GuiButton button : buttonList) {
            if (button.id >= values().length) {
                // This button is not a part of the gui, skip it.
                continue;
            }
            EnumInfuserButton buttonType = EnumInfuserButton.values()[button.id];
            button.displayString = I18n.format(buttonType.getButtonText(this.tileEntity));
            button.enabled = buttonType.buttonEnabled(this.tileEntity);
        }
    }
}
