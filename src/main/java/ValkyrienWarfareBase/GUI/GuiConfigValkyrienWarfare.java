package ValkyrienWarfareBase.GUI;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;

public class GuiConfigValkyrienWarfare extends GuiConfig {

	public GuiConfigValkyrienWarfare(GuiScreen parent) {
		super(parent, new ConfigElement(ValkyrienWarfareMod.config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(), ValkyrienWarfareMod.MODID, false, false, "Press Alt+F4 to enable debug features");
		titleLine2 = ValkyrienWarfareMod.configFile.getAbsolutePath();
	}

	@Override
	public void initGui() {
		// You can add buttons and initialize fields here
		super.initGui();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		// You can do things like create animations, draw additional elements, etc. here
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		// You can process any additional buttons you may have added here
		super.actionPerformed(button);
		ValkyrienWarfareMod.applyConfig(ValkyrienWarfareMod.config);
		ValkyrienWarfareMod.config.save();
	}

}