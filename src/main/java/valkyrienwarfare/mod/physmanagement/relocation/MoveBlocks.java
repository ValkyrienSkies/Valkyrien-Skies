package valkyrienwarfare.mod.physmanagement.relocation;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import valkyrienwarfare.addon.control.nodenetwork.IVWNodeProvider;
import valkyrienwarfare.physics.management.PhysicsObject;

import java.util.Optional;

public class MoveBlocks {

    /**
     * @param world
     * @param oldPos
     * @param newPos
     * @param physicsObjectOptional Used when we're using this to copy from world to physics object; should be empty when other way around.
     */
    public static void copyBlockToPos(World world, BlockPos oldPos, BlockPos newPos, Optional<PhysicsObject> physicsObjectOptional) {
        // To avoid any updates crap, just edit the chunk data array directly.
        // These look switched, but trust me they aren't
        IBlockState oldState = world.getBlockState(newPos);
        IBlockState newState = world.getBlockState(oldPos);
        // A hacky way to set the block state within the chunk while avoiding any block updates.
        Chunk chunkToSet = world.getChunk(newPos);
        int storageIndex = newPos.getY() >> 4;
        // Check that we're placing the block in a valid position
        if (storageIndex < 0 || storageIndex >= chunkToSet.storageArrays.length) {
            // Invalid position, abort!
            return;
        }
        if (chunkToSet.storageArrays[storageIndex] == Chunk.NULL_BLOCK_STORAGE) {
            chunkToSet.storageArrays[storageIndex] = new ExtendedBlockStorage(storageIndex << 4, true);
        }
        chunkToSet.storageArrays[storageIndex].set(newPos.getX() & 15, newPos.getY() & 15, newPos.getZ() & 15, newState);
        // Only want to send the update to clients and nothing else, so we use flag 2.
        world.notifyBlockUpdate(newPos, oldState, newState, 2);
        // Pretty messy to put this here but it works. Basically the ship keeps track of which of its chunks are
        // actually being used for performance reasons.
        if (physicsObjectOptional.isPresent()) {
            int minChunkX = physicsObjectOptional.get()
                    .getOwnedChunks()
                    .getMinX();
            int minChunkZ = physicsObjectOptional.get()
                    .getOwnedChunks()
                    .getMinZ();
            physicsObjectOptional.get()
                    .getOwnedChunks().chunkOccupiedInLocal[(newPos.getX() >> 4) - minChunkX][(newPos.getZ() >> 4) - minChunkZ] = true;
        }
        // Now that we've copied the block to the position, copy the tile entity
        copyTileEntityToPos(world, oldPos, newPos, physicsObjectOptional);
    }

    private static void copyTileEntityToPos(World world, BlockPos oldPos, BlockPos newPos, Optional<PhysicsObject> physicsObjectOptional) {
        // Make a copy of the tile entity at oldPos to newPos
        TileEntity worldTile = world.getTileEntity(oldPos);
        if (worldTile != null) {
            NBTTagCompound tileEntNBT = new NBTTagCompound();
            tileEntNBT = worldTile.writeToNBT(tileEntNBT);
            // Change the block position to be inside of the Ship
            tileEntNBT.setInteger("x", newPos.getX());
            tileEntNBT.setInteger("y", newPos.getY());
            tileEntNBT.setInteger("z", newPos.getZ());

            TileEntity newInstance = TileEntity.create(world, tileEntNBT);
            // Order the IVWNodeProvider to move by the given offset.
            if (newInstance != null && newInstance instanceof IVWNodeProvider) {
                ((IVWNodeProvider) newInstance).shiftInternalData(newPos.subtract(oldPos));
                if (physicsObjectOptional.isPresent()) {
                    ((IVWNodeProvider) newInstance)
                            .getNode()
                            .setParentPhysicsObject(physicsObjectOptional.get());
                } else {
                    ((IVWNodeProvider) newInstance)
                            .getNode()
                            .setParentPhysicsObject(null);
                }
            }

            try {
                newInstance.validate();
                world.setTileEntity(newPos, newInstance);
                if (physicsObjectOptional.isPresent()) {
                    physicsObjectOptional.get()
                            .onSetTileEntity(newPos, newInstance);
                }
                newInstance.markDirty();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
}
