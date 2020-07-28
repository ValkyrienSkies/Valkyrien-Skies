package org.valkyrienskies.addon.control.container;

import java.util.function.Function;
import org.valkyrienskies.addon.control.tileentity.TileEntityPhysicsInfuser;

public enum EnumInfuserButton {

    ASSEMBLE_SHIP("gui.assemble_ship", "gui.disassemble_ship",
        (tileEntity) -> !tileEntity.isCurrentlyInShip(),
        (tileEntity) -> tileEntity.canMaintainShip() && tileEntity.canShipBeDeconstructed()
            && tileEntity.isCenterOfShip()),
    ENABLE_PHYSICS("gui.enable_physics", "gui.disable_physics",
        (tileEntity) -> !tileEntity.isPhysicsEnabled(),
        (tileEntity -> tileEntity.canMaintainShip() && tileEntity.isCurrentlyInShip() && tileEntity
            .isCenterOfShip())),
    ALIGN_SHIP("gui.align_ship", "gui.stop_align_ship",
        (tileEntity) -> !tileEntity.isTryingToAlignShip(),
        tileEntity -> tileEntity.canMaintainShip() && tileEntity.isCurrentlyInShip() && tileEntity
            .isCenterOfShip());

    private final String trueMessage, falseMessage;
    private final Function<TileEntityPhysicsInfuser, Boolean> messageDiscriminator, buttonEnabled;

    EnumInfuserButton(String trueMessage, String falseMessage,
        Function<TileEntityPhysicsInfuser, Boolean> messageDiscriminator,
        Function<TileEntityPhysicsInfuser, Boolean> buttonEnabled) {
        this.trueMessage = trueMessage;
        this.falseMessage = falseMessage;
        this.messageDiscriminator = messageDiscriminator;
        this.buttonEnabled = buttonEnabled;
    }

    public String getButtonText(TileEntityPhysicsInfuser tileEntity) {
        return messageDiscriminator.apply(tileEntity) ? trueMessage : falseMessage;
    }

    public boolean buttonEnabled(TileEntityPhysicsInfuser tileEntity) {
        return buttonEnabled.apply(tileEntity);
    }
}
