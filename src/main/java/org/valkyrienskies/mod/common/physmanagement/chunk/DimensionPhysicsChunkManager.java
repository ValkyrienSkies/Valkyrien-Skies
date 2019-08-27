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

package org.valkyrienskies.mod.common.physmanagement.chunk;

import net.minecraft.world.World;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;
import org.valkyrienskies.mod.common.physmanagement.interaction.QueryableShipData;
import org.valkyrienskies.mod.common.physmanagement.interaction.ShipPositionData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DimensionPhysicsChunkManager {

    private final Map<World, PhysicsChunkManager> managerPerWorld;

    public DimensionPhysicsChunkManager() {
        managerPerWorld = new HashMap<>();
    }

    public void initWorld(World world) {
        if (!managerPerWorld.containsKey(world)) {
            System.out.println("Physics Chunk Manager Initialized");
            managerPerWorld.put(world, new PhysicsChunkManager(world));
        }
    }

    public PhysicsChunkManager getManagerForWorld(World world) {
        return managerPerWorld.get(world);
    }

    public void removeWorld(World world) {
        managerPerWorld.remove(world);
    }

    public void registerChunksForShip(PhysicsWrapperEntity wrapper) {
        QueryableShipData.get(wrapper.world).addShip(wrapper);
    }

    public void removeRegisteredChunksForShip(PhysicsWrapperEntity wrapper) {
        QueryableShipData.get(wrapper.world).removeShip(wrapper);
    }

    public UUID getShipIDManagingPos_Persistent(World worldFor, int chunkX, int chunkZ) {
        return QueryableShipData.get(worldFor).getShipUUIDFromPos(chunkX, chunkZ);
    }

    public ShipPositionData getShipPosition_Persistent(World worldFor, UUID shipID) {
        return QueryableShipData.get(worldFor).getShip(shipID).get().positionData;

    }

    public void updateShipPosition(PhysicsWrapperEntity wrapper) {
        QueryableShipData.get(wrapper.world).updateShipPosition(wrapper);
    }

    public void removeShipPosition(PhysicsWrapperEntity wrapper) {
        QueryableShipData.get(wrapper.world).removeShip(wrapper);
    }

    public void removeShipNameRegistry(PhysicsWrapperEntity wrapper) {
        QueryableShipData.get(wrapper.world).removeShip(wrapper);
    }
}
