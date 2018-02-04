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

package valkyrienwarfare.physics.calculations;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physics.management.PhysicsObject;

import java.util.ArrayList;
import java.util.List;

public class ShipFluidProcessor {

    public final PhysicsObject parent;
    public ArrayList<BlockPos> submergedAirPositions = new ArrayList<BlockPos>();
    public ArrayList<BlockPos>[] solidPositionsAtYLevel = new ArrayList[256];

    public ShipFluidProcessor(PhysicsObject parent) {
        this.parent = parent;
    }

    public void updateSubmergedPositionsCalculations(float maxTime) {
        int yOceanLevel = getWaterLevelAtShip();
        Vector shipUpNormal = new Vector(0D, 1D, 0D, parent.coordTransform.lToWRotation);

        int maxYToCheck = yOceanLevel;
        int minYToCheck = Math.max(MathHelper.floor(parent.wrapper.getEntityBoundingBox().minY), 0);

        for (int currentY = maxYToCheck; currentY > minYToCheck; currentY++) {

        }
    }

    private int getWaterLevelAtShip() {
        return 45;
    }

    private boolean doesStateBlockWater(IBlockState state) {
        return state.isFullBlock();
    }

    public void generateYLevelData() {
        for (BlockPos pos : parent.blockPositions) {
            IBlockState state = parent.VKChunkCache.getBlockState(pos);
            if (doesStateBlockWater(state)) {
                int yPos = pos.getY();
                ArrayList<BlockPos> dataAtY = solidPositionsAtYLevel[yPos];
                if (dataAtY == null) {
                    dataAtY = new ArrayList<>();
                    solidPositionsAtYLevel[yPos] = dataAtY;
                }
                dataAtY.add(pos);
            }
        }
    }

    public void onSetBlockState(BlockPos pos, IBlockState oldState, IBlockState newState) {
        int yPos = pos.getY();
        ArrayList<BlockPos> dataAtY = solidPositionsAtYLevel[yPos];
        if (dataAtY == null) {
            dataAtY = new ArrayList<>();
            solidPositionsAtYLevel[yPos] = dataAtY;
        }
        boolean doesOldStateBlockWater = doesStateBlockWater(oldState);
        boolean doesNewStateBlockWater = doesStateBlockWater(newState);
        if (doesOldStateBlockWater != doesNewStateBlockWater) {
            if (doesNewStateBlockWater) {
                dataAtY.add(pos);
            } else {
                dataAtY.remove(pos);
            }
        }
    }

    class NestedCrossSection {
        public final ArrayList<BlockPos> normalizedSolidPositions = new ArrayList<BlockPos>();
        public final ArrayList<BlockPos> airPositions = new ArrayList<BlockPos>();
        //A 2-dimensional shape made of the solid block positions
        final int yLevelToSimulate;
        final Vector shipUpNormalVector;
        private final AxisAlignedBB shipBB;
        private final ArrayList<BlockPos>[] solidPositionsAtYLevel;
        private final double[] lToWTransform;
        public List<BlockPos> blockPositionsOfCrossSection;

        public NestedCrossSection(int yLevelToSimulate, Vector shipUpNormalVector, ArrayList<BlockPos>[] solidPositionsAtYLevel, AxisAlignedBB shipBB, double[] lToWTransform) {
            this.yLevelToSimulate = yLevelToSimulate;
            this.shipUpNormalVector = shipUpNormalVector;
            this.solidPositionsAtYLevel = solidPositionsAtYLevel;
            this.shipBB = shipBB;
            this.lToWTransform = lToWTransform;
        }

        public void runSimulation() {

        }
    }
}
