/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2019 the Valkyrien Skies team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Skies team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Skies team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package org.valkyrienskies.addon.control.block.engine;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.valkyrienskies.addon.control.tileentity.TileEntityPropellerEngine;
import org.valkyrienskies.mod.common.coordinates.VectorImmutable;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The same as a normal engine, but says speed in the tooltip
 */
public abstract class BlockAirshipEngineLore extends BlockAirshipEngine {

    private String[] lore;

    public BlockAirshipEngineLore(String name, Material mat, double enginePower, float hardness) {
        super(name, mat, enginePower, hardness);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player,
        List<String> itemInformation, ITooltipFlag advanced) {
        Collections.addAll(itemInformation, lore);
    }

    public abstract String getEnginePowerTooltip();

    @Override
    public void setEnginePower(double power) {
        super.setEnginePower(power);
        lore = new String[]{
            "" + TextFormatting.GRAY + TextFormatting.ITALIC + TextFormatting.BOLD + "Force:",
            "  " + this.getEnginePowerTooltip() + " Newtons"};
    }

    @Override
    public boolean shouldLocalForceBeRotated(World world, BlockPos pos, IBlockState state,
        double secondsToApply) {
        return true;
    }

    @Override
    public Vector getCustomBlockForcePosition(World world, BlockPos pos, IBlockState state,
        PhysicsObject physicsObject,
        double secondsToApply) {
        TileEntityPropellerEngine engineTile = (TileEntityPropellerEngine) world.getTileEntity(pos);
        if (engineTile != null) {
            VectorImmutable forceOutputNormal = engineTile.getForceOutputNormal(secondsToApply,
                physicsObject);
            return new Vector(pos.getX() + .5D - forceOutputNormal.getX() * .75,
                pos.getY() + .5D - forceOutputNormal.getY() * .75,
                pos.getZ() + .5D - forceOutputNormal.getZ() * .75);
        } else {
            return null;
        }
    }

}
