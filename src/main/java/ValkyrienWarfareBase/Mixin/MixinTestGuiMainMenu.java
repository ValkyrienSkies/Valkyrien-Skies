package ValkyrienWarfareBase.Mixin;

import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiMainMenu.class)
public abstract class MixinTestGuiMainMenu extends GuiScreen {
    /**
     * The splash text to display
     * @Shadow means that the string isn't in this class, but is in GuiMainMenu and therefore stuff WILL work
     */
    @Shadow
    private String splashText;

    /**
     * everything in this method is injected into GuiMainMenu's initGui method, right before the end
     */
    @Inject(method = "initGui", at = @At("RETURN"))
    public void letsInjectThis(CallbackInfo callbackInfo)   {
        this.splashText = "valkyrien warfare mixins test!!!";
    }
}
//TODO: delete this
