/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.addon.control.controlsystems.controlgui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;
import valkyrienwarfare.addon.control.network.ThrustModulatorGuiInputMessage;
import valkyrienwarfare.addon.control.tileentity.TileEntityThrustModulator;

public class ThrustModulatorGui extends GuiScreen {

    private static final ResourceLocation BACKGROUND = new ResourceLocation("valkyrienwarfarecontrol",
            "textures/gui/thrustmodulator.png");
    private final TileEntityThrustModulator tileEnt;
    private final List<GuiTextField> textFields;

    public ThrustModulatorGui(EntityPlayer player, TileEntityThrustModulator entity) {
        mc = Minecraft.getMinecraft();
        tileEnt = entity;
        textFields = new ArrayList<GuiTextField>();
    }

    public void updateTextFields() {
        textFields.get(0).setText("" + tileEnt.idealYHeight); // Top button
        textFields.get(1).setText("" + tileEnt.maximumYVelocity); // Middle button
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
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
        if (tileEnt != null && NumberUtils.isCreatable(textFields.get(0).getText())
                && NumberUtils.isCreatable(textFields.get(1).getText())) {
            float data = Float.parseFloat(textFields.get(0).getText());
            float data2 = Float.parseFloat(textFields.get(1).getText());
            ThrustModulatorGuiInputMessage toSend = new ThrustModulatorGuiInputMessage(tileEnt.getPos(), data, data2);
            ValkyrienWarfareControl.controlNetwork.sendToServer(toSend);
        }
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
        GuiTextField top = new GuiTextField(0, fontRenderer, (width - fieldWidth) / 2 - 57,
                (height - fieldHeight) / 2 - 77, fieldWidth, fieldHeight);
        GuiTextField mid = new GuiTextField(0, fontRenderer, (width - fieldWidth) / 2 - 57,
                (height - fieldHeight) / 2 - 49, fieldWidth, fieldHeight);
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
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        super.drawScreen(par1, par2, par3);
        mc.getTextureManager().bindTexture(BACKGROUND);

        int textureWidth = 239;
        int textureHeight = 232;

        drawTexturedModalRect((width - textureWidth) / 2, (height - textureHeight) / 2, 7, 7, textureWidth,
                textureHeight);

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

}
