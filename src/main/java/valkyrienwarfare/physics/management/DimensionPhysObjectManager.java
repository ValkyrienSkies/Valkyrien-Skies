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

package valkyrienwarfare.physics.management;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.mod.physmanagement.chunk.PhysicsChunkManager;

public class DimensionPhysObjectManager {

    private final Map<World, WorldPhysObjectManager> managerPerWorld;
    private WorldPhysObjectManager cachedManager;

    public DimensionPhysObjectManager() {
        managerPerWorld = new HashMap<World, WorldPhysObjectManager>();
    }

    /**
     * Kinda like a preorder, order one now!
     *
     * @param toPreload
     */
    public void onShipPreload(PhysicsWrapperEntity toPreload) {
        getManagerForWorld(toPreload.world).preloadPhysicsWrapperEntityMappings(toPreload);
    }

    // Put the ship in the manager queues
    public void onShipLoad(PhysicsWrapperEntity justLoaded) {
        getManagerForWorld(justLoaded.world).onLoad(justLoaded);
    }

    // Remove the ship from the damn queues
    public void onShipUnload(PhysicsWrapperEntity justUnloaded) {
        getManagerForWorld(justUnloaded.world).onUnload(justUnloaded);
    }

    public void initWorld(World toInit) {
        if (!managerPerWorld.containsKey(toInit)) {
            managerPerWorld.put(toInit, new WorldPhysObjectManager(toInit));
        }
    }

    public WorldPhysObjectManager getManagerForWorld(World world) {
        if (world == null) {
            //I'm not quite sure what to do here
        }
        if (cachedManager == null || cachedManager.worldObj != world) {
            cachedManager = managerPerWorld.get(world);
        }
        if (cachedManager == null) {
            System.err.println("getManagerForWorld just requested for a World without one!!! Assuming that this is a new world, so making a new WorldPhysObjectManager for it.");
            cachedManager = new WorldPhysObjectManager(world);
            //Make sure to add the cachedManager to the world managers
            managerPerWorld.put(world, cachedManager);
        }
        return cachedManager;
    }

    public void removeWorld(World world) {
        if (managerPerWorld.containsKey(world)) {
            getManagerForWorld(world).physicsEntities.clear();
        }
        managerPerWorld.remove(world);
    }

    /**
     * Returns the PhysicsWrapperEntity that claims this chunk if there is one; returns null if there is no loaded entity managing it
     */

    //TODO: Fix this
    @Deprecated
    public PhysicsWrapperEntity getObjectManagingPos(World world, BlockPos pos) {
        if (world == null || pos == null) {
            return null;
        }
        if (world.getChunkProvider() == null) {
//			System.out.println("Retard Devs coded a World with no Chunks in it!");
            return null;
        }

        if (!PhysicsChunkManager.isLikelyShipChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
            return null;
        }
        //NoClassFound entity$1.class FIX
//		if(!world.isRemote){
//			if(world.getChunkProvider() instanceof ChunkProviderServer){
//				ChunkProviderServer providerServer =  (ChunkProviderServer) world.getChunkProvider();
//				//The chunk at the given pos isn't loaded? Don't bother with the next step, you'll create an infinite loop!
//				if(!providerServer.chunkExists(pos.getX() >> 4, pos.getZ() >> 4)){
//					return null;
//				}
//			}
//		}
//		Chunk chunk = world.getChunkFromBlockCoords(pos);
//		return getObjectManagingChunk(chunk);
        WorldPhysObjectManager physManager = getManagerForWorld(world);
        if (physManager == null) {
            return null;
        }
        return physManager.getManagingObjectForChunkPosition(pos.getX() >> 4, pos.getZ() >> 4);
    }

    public boolean isEntityFixed(Entity entity) {
        return getManagerForWorld(entity.world).isEntityFixed(entity);
    }

    public PhysicsWrapperEntity getShipFixedOnto(Entity entity) {
        return getManagerForWorld(entity.world).getShipFixedOnto(entity, false);
    }

}
