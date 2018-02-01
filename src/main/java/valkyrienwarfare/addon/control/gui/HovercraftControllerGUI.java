/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2017 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package valkyrienwarfare.addon.control.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;
import valkyrienwarfare.addon.control.network.HovercraftControllerGUIInputMessage;
import valkyrienwarfare.addon.control.tileentity.TileEntityHoverController;

import java.io.IOException;
import java.util.ArrayList;

public class HovercraftControllerGUI extends GuiContainer {

    private static ResourceLocation background = new ResourceLocation("valkyrienwarfarecontrol", "textures/gui/controlsystemgui.png");
    public TileEntityHoverController tileEnt;
    public ArrayList<GuiTextField> textFields = new ArrayList<GuiTextField>();

    public HovercraftControllerGUI(EntityPlayer player, TileEntityHoverController entity) {
        super(player.inventoryContainer);
        mc = Minecraft.getMinecraft();
        tileEnt = entity;
    }

    public void updateTextFields() {
        textFields.get(0).setText(tileEnt.idealHeight + ""); // TOP
        textFields.get(1).setText(Math.round(tileEnt.stabilityBias * 100D) + ""); // MID
        textFields.get(2).setText(tileEnt.linearVelocityBias + ""); // BOT
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        // super.mouseClicked(mouseX,mouseY,mouseButton);
        boolean prevFocused = false;
        boolean postFocused = false;
        for (GuiTextField text : textFields) {
            prevFocused = text.isFocused() || prevFocused;
            text.mouseClicked(mouseX, mouseY, mouseButton);
            postFocused = text.isFocused() || postFocused;
        }
        if (prevFocused && !postFocused) {
            updateServer();
        }

    }

    public void updateServer() {
        if (tileEnt == null) {
            return;
        }
        HovercraftControllerGUIInputMessage toSend = getMessage();
        ValkyrienWarfareControl.controlNetwork.sendToServer(toSend);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        boolean typed = false;
        for (GuiTextField text : textFields) {
            typed = typed || text.textboxKeyTyped(typedChar, keyCode);
        }
        if (!typed) {
            updateServer();
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        for (GuiTextField text : textFields) {
            text.updateCursorCounter();
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        this.mc.player.openContainer = this.inventorySlots;
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
        textFields.clear();
        int fieldWidth = 40;
        int fieldHeight = 20;
        GuiTextField top = new GuiTextField(0, fontRenderer, (width - fieldWidth) / 2 - 61, (height - fieldHeight) / 2 - 77, fieldWidth, fieldHeight);
        GuiTextField mid = new GuiTextField(0, fontRenderer, (width - fieldWidth) / 2 - 57, (height - fieldHeight) / 2 - 49, fieldWidth, fieldHeight);
        GuiTextField bot = new GuiTextField(0, fontRenderer, (width - fieldWidth) / 2 - 57, (height - fieldHeight) / 2 - 20, fieldWidth, fieldHeight);
        top.setEnableBackgroundDrawing(false);
        mid.setEnableBackgroundDrawing(false);
        bot.setEnableBackgroundDrawing(false);
        textFields.add(top);
        textFields.add(mid);
        textFields.add(bot);
        updateTextFields();
        // buttonList.add(new GuiButton(1, width/2-100, height/2-24, "Bastard button"));
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        updateServer();
        Keyboard.enableRepeatEvents(false);
        // Minecraft.getMinecraft().thePlayer.closeScreenAndDropStack();
        // Minecraft.getMinecraft().thePlayer.cra
        // this.inventorySlots.removeCraftingFromCrafters(this);
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        // super.drawScreen(par1, par2, par3);
        // drawBackground(par1);
        drawGuiContainerBackgroundLayer(par3, par1, par2);
        for (int i = 0; i < this.buttonList.size(); ++i) {
            ((GuiButton) this.buttonList.get(i)).drawButton(this.mc, par1, par2, par3);
        }

        for (int j = 0; j < this.labelList.size(); ++j) {
            this.labelList.get(j).drawLabel(this.mc, par1, par2);
        }
        for (GuiTextField text : textFields) {
            text.drawTextBox();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        // GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(background);

        //int textureWidth = 332;
        //int textureHeight = 352;
        int textureWidth = 239;
        int textureHeight = 232;

        drawTexturedModalRect((width - textureWidth) / 2, (height - textureHeight) / 2, 7, 7, textureWidth, textureHeight);
    }

    @Override
    public void drawBackground(int tint) {
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldrenderer = tessellator.getBuffer();
        this.mc.getTextureManager().bindTexture(background);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        float f = 32.0F;
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldrenderer.pos(0.0D, (double) this.height, 0.0D).tex(0.0D, (double) ((float) this.height / 32.0F + (float) tint)).color(64, 64, 64, 255).endVertex();
        worldrenderer.pos((double) this.width, (double) this.height, 0.0D).tex((double) ((float) this.width / 32.0F), (double) ((float) this.height / 32.0F + (float) tint)).color(64, 64, 64, 255).endVertex();
        worldrenderer.pos((double) this.width, 0.0D, 0.0D).tex((double) ((float) this.width / 32.0F), (double) tint).color(64, 64, 64, 255).endVertex();
        worldrenderer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, (double) tint).color(64, 64, 64, 255).endVertex();
        tessellator.draw();
    }

    private HovercraftControllerGUIInputMessage getMessage() {
        HovercraftControllerGUIInputMessage toReturn = new HovercraftControllerGUIInputMessage();
        toReturn.tilePos = tileEnt.getPos();
        toReturn.physEntId = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(mc.world, toReturn.tilePos).getEntityId();
        try {
            toReturn.newIdealHeight = Double.parseDouble(textFields.get(0).getText());
            toReturn.newStablitiyBias = Double.parseDouble(textFields.get(1).getText()) / 100D;
            toReturn.newLinearVelocityBias = Double.parseDouble(textFields.get(2).getText());
        } catch (Exception e) {
            updateTextFields();
        }
        return toReturn;
    }

}