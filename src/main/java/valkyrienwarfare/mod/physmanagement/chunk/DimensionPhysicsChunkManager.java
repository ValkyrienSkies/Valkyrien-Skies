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

package valkyrienwarfare.mod.physmanagement.chunk;

import net.minecraft.world.World;
import valkyrienwarfare.mod.physmanagement.interaction.BlockPosToShipUUIDData;
import valkyrienwarfare.mod.physmanagement.interaction.ShipNameUUIDData;
import valkyrienwarfare.mod.physmanagement.interaction.ShipUUIDToPosData;
import valkyrienwarfare.mod.physmanagement.interaction.ShipUUIDToPosData.ShipPositionData;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DimensionPhysicsChunkManager {

    private final Map<World, PhysicsChunkManager> managerPerWorld;

    public DimensionPhysicsChunkManager() {
        managerPerWorld = new HashMap<World, PhysicsChunkManager>();
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
        World shipWorld = wrapper.world;
        BlockPosToShipUUIDData data = BlockPosToShipUUIDData.get(shipWorld);
        data.addShipToPersistantMap(wrapper);
    }

    public void removeRegistedChunksForShip(PhysicsWrapperEntity wrapper) {
        World shipWorld = wrapper.world;
        BlockPosToShipUUIDData data = BlockPosToShipUUIDData.get(shipWorld);

        data.removeShipFromPersistantMap(wrapper);
    }

    public UUID getShipIDManagingPos_Persistant(World worldFor, int chunkX, int chunkZ) {
        BlockPosToShipUUIDData data = BlockPosToShipUUIDData.get(worldFor);

        return data.getShipUUIDFromPos(chunkX, chunkZ);
    }

    public ShipPositionData getShipPosition_Persistant(World worldFor, UUID shipID) {
        ShipUUIDToPosData data = ShipUUIDToPosData.getShipUUIDDataForWorld(worldFor);

        return data.getShipPositionData(shipID);
    }

    public void updateShipPosition(PhysicsWrapperEntity wrapper) {
        World shipWorld = wrapper.world;
        ShipUUIDToPosData data = ShipUUIDToPosData.getShipUUIDDataForWorld(shipWorld);

        data.updateShipPosition(wrapper);
    }

    public void removeShipPosition(PhysicsWrapperEntity wrapper) {
        World shipWorld = wrapper.world;
        ShipUUIDToPosData data = ShipUUIDToPosData.getShipUUIDDataForWorld(shipWorld);

        data.removeShipFromMap(wrapper);
    }

    public void removeShipNameRegistry(PhysicsWrapperEntity wrapper) {
        World shipWorld = wrapper.world;
        ShipNameUUIDData data = ShipNameUUIDData.get(shipWorld);

        data.removeShipFromRegistry(wrapper);
    }
}
