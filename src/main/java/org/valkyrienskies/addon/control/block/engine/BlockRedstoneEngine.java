package org.valkyrienskies.addon.control.block.engine;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.physics.management.PhysicsObject;

public class BlockRedstoneEngine extends BlockAirshipEngineLore {

    public BlockRedstoneEngine(Material materialIn, double powerMultiplier) {
        super(materialIn, powerMultiplier);
    }

    @Override
    public double getEnginePower(World world, BlockPos pos, IBlockState state,
        PhysicsObject physicsObject) {
        // Fixes "It seems like redstone engines have been nerfed". ~Del
        return world.getRedstonePowerFromNeighbors(pos) * this.enginePower;
    }

    @Override
    public String getEnginePowerTooltip() {
        return enginePower + " * redstone power level";
    }

}
