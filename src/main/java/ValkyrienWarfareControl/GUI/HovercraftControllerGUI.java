package ValkyrienWarfareControl.GUI;

import java.io.IOException;
import java.util.ArrayList;

import org.lwjgl.input.Keyboard;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareControl.ValkyrienWarfareControlMod;
import ValkyrienWarfareControl.Network.HovercraftControllerGUIInputMessage;
import ValkyrienWarfareControl.TileEntity.TileEntityHoverController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class HovercraftControllerGUI extends GuiContainer {

	private static ResourceLocation background = new ResourceLocation(ValkyrienWarfareControlMod.MODID.toLowerCase(), "textures/gui/ControlSystemGUI.png");
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
		ValkyrienWarfareControlMod.controlNetwork.sendToServer(toSend);
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
		this.mc.thePlayer.openContainer = this.inventorySlots;
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;
		textFields.clear();
		int fieldWidth = 40;
		int fieldHeight = 20;
		GuiTextField top = new GuiTextField(0, fontRendererObj, (width - fieldWidth) / 2 - 61, (height - fieldHeight) / 2 - 77, fieldWidth, fieldHeight);
		GuiTextField mid = new GuiTextField(0, fontRendererObj, (width - fieldWidth) / 2 - 57, (height - fieldHeight) / 2 - 49, fieldWidth, fieldHeight);
		GuiTextField bot = new GuiTextField(0, fontRendererObj, (width - fieldWidth) / 2 - 57, (height - fieldHeight) / 2 - 20, fieldWidth, fieldHeight);
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
			((GuiButton) this.buttonList.get(i)).drawButton(this.mc, par1, par2);
		}

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

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		// GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(background);
		int textureX = 7;
		int textureY = 7;

		int textureWidth = 239;
		int textureHeight = 232;

		drawTexturedModalRect((width - textureWidth) / 2, (height - textureHeight) / 2, textureX, textureY, textureWidth, textureHeight);
	}

	@Override
	public void drawBackground(int tint) {
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer worldrenderer = tessellator.getBuffer();
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
		toReturn.physEntId = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(mc.theWorld, toReturn.tilePos).getEntityId();
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