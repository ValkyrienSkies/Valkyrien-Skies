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

package valkyrienwarfare.addon.control.controlsystems.controlgui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;
import valkyrienwarfare.addon.control.network.ThrustModulatorGuiInputMessage;
import valkyrienwarfare.addon.control.tileentity.ThrustModulatorTileEntity;

import java.io.IOException;
import java.util.ArrayList;

public class ThrustModulatorGui extends GuiScreen {

    private static ResourceLocation background = new ResourceLocation("valkyrienwarfarecontrol", "textures/gui/thrustmodulator.png");
    public ThrustModulatorTileEntity tileEnt;
    public ArrayList<GuiTextField> textFields = new ArrayList<GuiTextField>();

    public ThrustModulatorGui(EntityPlayer player, ThrustModulatorTileEntity entity) {
        super();
        mc = Minecraft.getMinecraft();
        tileEnt = entity;
    }

    public void updateTextFields() {
        textFields.get(0).setText(tileEnt.idealYHeight + ""); // TOP
        textFields.get(1).setText(tileEnt.maximumYVelocity + ""); // MID
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
        ThrustModulatorGuiInputMessage toSend = new ThrustModulatorGuiInputMessage(tileEnt.getPos(), Float.parseFloat(textFields.get(0).getText()), Float.parseFloat(textFields.get(1).getText()));
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
        textFields.clear();
        int fieldWidth = 40;
        int fieldHeight = 20;
        GuiTextField top = new GuiTextField(0, fontRenderer, (width - fieldWidth) / 2 - 57, (height - fieldHeight) / 2 - 77, fieldWidth, fieldHeight);
        GuiTextField mid = new GuiTextField(0, fontRenderer, (width - fieldWidth) / 2 - 57, (height - fieldHeight) / 2 - 49, fieldWidth, fieldHeight);
        top.setEnableBackgroundDrawing(false);
        mid.setEnableBackgroundDrawing(false);
        textFields.add(top);
        textFields.add(mid);
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
        super.drawScreen(par1, par2, par3);
        mc.getTextureManager().bindTexture(background);

        //int textureWidth = 332;
        //int textureHeight = 352;
        int textureWidth = 239;
        int textureHeight = 232;

        drawTexturedModalRect((width - textureWidth) / 2, (height - textureHeight) / 2, 7, 7, textureWidth, textureHeight);

        for (int j = 0; j < this.labelList.size(); ++j) {
            ((GuiLabel) this.labelList.get(j)).drawLabel(this.mc, par1, par2);
        }
        for (GuiTextField text : textFields) {
            text.drawTextBox();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

}
