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

package valkyrienwarfare.mod.common.physmanagement.interaction;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import valkyrienwarfare.mod.common.physics.management.PhysicsWrapperEntity;
import valkyrienwarfare.mod.common.physmanagement.chunk.VWChunkClaim;
import valkyrienwarfare.mod.common.util.NBTUtils;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

public class BlockPosToShipUUIDData extends WorldSavedData {

    private static final String key = "BlockPosToShipUUIDData";
    //Not the persistent map, used for performance reasons
    private TLongObjectMap<UUID> chunkposToShipUUID = new TLongObjectHashMap<>();
    private Map<UUID, VWChunkClaim> UUIDToChunkSet = new HashMap<>();

    public BlockPosToShipUUIDData(String name) {
        super(name);
    }

    public BlockPosToShipUUIDData() {
        super(key);
    }

    public static BlockPosToShipUUIDData get(World world) {
        MapStorage storage = world.getPerWorldStorage();
        BlockPosToShipUUIDData data = (BlockPosToShipUUIDData) storage.getOrLoadData(BlockPosToShipUUIDData.class, key);
        if (data == null) {
            data = new BlockPosToShipUUIDData();
            world.setData(key, data);
        }
        return data;
    }

    public UUID getShipUUIDFromPos(int chunkX, int ChunkZ) {
        long chunkPos = ChunkPos.asLong(chunkX, ChunkZ);

        return chunkposToShipUUID.get(chunkPos);
    }

    public void addShipToPersistantMap(PhysicsWrapperEntity toAdd) {
        UUID shipID = toAdd.getPersistentID();

        int centerX = toAdd.getPhysicsObject().getOwnedChunks().getCenterX();
        int centerZ = toAdd.getPhysicsObject().getOwnedChunks().getCenterZ();
        int radius = toAdd.getPhysicsObject().getOwnedChunks().getRadius();

        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                long chunkPos = ChunkPos.asLong(x, z);
                chunkposToShipUUID.put(chunkPos, shipID);
            }
        }
        UUIDToChunkSet.put(toAdd.getPersistentID(), toAdd.getPhysicsObject().getOwnedChunks());
        markDirty();
    }

    public void removeShipFromPersistantMap(PhysicsWrapperEntity toRemove) {
        int centerX = toRemove.getPhysicsObject().getOwnedChunks().getCenterX();
        int centerZ = toRemove.getPhysicsObject().getOwnedChunks().getCenterZ();
        int radius = toRemove.getPhysicsObject().getOwnedChunks().getRadius();

        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                long chunkPos = ChunkPos.asLong(x, z);
                chunkposToShipUUID.remove(chunkPos);
            }
        }
        UUIDToChunkSet.remove(toRemove.getPersistentID());
        markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        ByteBuffer buffer = NBTUtils.getByteBuf("WorldChunkSetUUIDMix", compound);

//		buffer.flip();
        while (buffer.hasRemaining()) {

            int centerX = buffer.getInt();
            int centerZ = buffer.getInt();
            byte radius = buffer.get();
            long mostBits = buffer.getLong();
            long leastBits = buffer.getLong();

//			System.out.println("Loaded a ChunkSet at " + centerX + ":" + centerZ);

            UUID persistantID = new UUID(mostBits, leastBits);
            VWChunkClaim set = new VWChunkClaim(centerX, centerZ, radius);

            UUIDToChunkSet.put(persistantID, set);

            for (int x = centerX - radius; x <= centerX + radius; x++) {
                for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                    chunkposToShipUUID.put(ChunkPos.asLong(x, z), persistantID);
                }
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        Set<Entry<UUID, VWChunkClaim>> entries = UUIDToChunkSet.entrySet();

        //2 ints, 1 byte (radius), and 2 longs for each ship, that comes out to 25 bytes per entry
        int byteArraySize = entries.size() * 25;
        ByteBuffer buffer = ByteBuffer.allocate(byteArraySize);
        for (Entry<UUID, VWChunkClaim> entry : entries) {
            int centerX = entry.getValue().getCenterX();
            int centerZ = entry.getValue().getCenterZ();
            byte radius = (byte) entry.getValue().getRadius();
            long mostBits = entry.getKey().getMostSignificantBits();
            long leastBits = entry.getKey().getLeastSignificantBits();

            buffer.putInt(centerX);
            buffer.putInt(centerZ);
            buffer.put(radius);
            buffer.putLong(mostBits);
            buffer.putLong(leastBits);
        }

        NBTUtils.setByteBuf("WorldChunkSetUUIDMix", buffer, compound);
        return compound;
    }

}
