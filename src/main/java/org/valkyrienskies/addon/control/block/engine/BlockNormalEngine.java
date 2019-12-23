package org.valkyrienskies.addon.control.block.engine;

import net.minecraft.block.material.Material;

public class BlockNormalEngine extends BlockAirshipEngineLore {

    public BlockNormalEngine(Material materialIn, double enginePower) {
        super(materialIn, enginePower);
    }

    @Override
    public String getEnginePowerTooltip() {
        return String.valueOf(enginePower);
    }
}