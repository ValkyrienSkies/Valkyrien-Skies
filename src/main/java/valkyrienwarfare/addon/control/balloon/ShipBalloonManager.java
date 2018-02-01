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

package valkyrienwarfare.addon.control.balloon;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import valkyrienwarfare.physicsmanagement.PhysicsObject;

import java.util.ArrayList;

public class ShipBalloonManager {

    public ArrayList<BalloonProcessor> balloonProcessors = new ArrayList<BalloonProcessor>();
    public ArrayList<BlockPos> recentBlockPositionChanges = new ArrayList<BlockPos>();
    public PhysicsObject parent;
    private int curBalloonTick;

    public ShipBalloonManager(PhysicsObject parent) {
        this.parent = parent;
    }

    // Searches 5 blocks up for a processor, if one cant be found then it returns null
    public BalloonProcessor getProcessorAbovePos(BlockPos burnerPos) {
        for (int i = 1; i <= 5; i++) {
            BlockPos toCheck = burnerPos.up(i);
            IBlockState state = parent.VKChunkCache.getBlockState(toCheck);
            Block block = state.getBlock();
            if (block.blockMaterial.blocksMovement()) {
                // End the loop
                i = 420;
            } else {
                for (BalloonProcessor processor : balloonProcessors) {
                    if (processor.isBlockPosInRange(toCheck)) {
                        if (processor.internalAirPositions.contains(toCheck)) {
                            return processor;
                        }
                    }
                }
            }
        }
        return null;
    }

    public void addBalloonProcessor(BalloonProcessor toAdd) {
        balloonProcessors.add(toAdd);
    }

    public void onPostTick() {
        curBalloonTick++;
        if (curBalloonTick > 20) {
            curBalloonTick = 0;
            processRecentBlockChanges();
            // System.out.println("updated");
        }
    }

    private void processRecentBlockChanges() {
        if (!recentBlockPositionChanges.isEmpty()) {
            for (BalloonProcessor processor : balloonProcessors) {
                processor.processBlockUpdates(recentBlockPositionChanges);
            }

            // System.out.println("Processed "+recentBlockPositionChanges.size()+" block changes");

            recentBlockPositionChanges.clear();
        }
    }

    public void onBlockPositionRemoved(BlockPos justRemoved) {
        if (!recentBlockPositionChanges.contains(justRemoved)) {
            recentBlockPositionChanges.add(justRemoved);
        }
    }

    public void onBlockPositionAdded(BlockPos justAdded) {
        if (!recentBlockPositionChanges.contains(justAdded)) {
            recentBlockPositionChanges.add(justAdded);
        }
    }

}
