package valkyrienwarfare.mod.physmanagement.relocation;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
        IBlockState state = world.getBlockState(oldPos);
        // Move pos to the ship space

        // To avoid any updates crap, just edit the chunk data array directly.

        /*
        Chunk chunkToSet = world.getChunkFromBlockCoords(newPos);
        int storageIndex = newPos.getY() >> 4;

        if (chunkToSet.storageArrays[storageIndex] == Chunk.NULL_BLOCK_STORAGE) {
            chunkToSet.storageArrays[storageIndex] = new ExtendedBlockStorage(storageIndex << 4, true);
        }
        chunkToSet.storageArrays[storageIndex].set(newPos.getX() & 15, newPos.getY() & 15, newPos.getZ() & 15, state);
         */
        world.setBlockState(newPos, world.getBlockState(oldPos), 16);

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
            newInstance.validate();

            world.setTileEntity(newPos, newInstance);
            if (physicsObjectOptional.isPresent()) {
                physicsObjectOptional.get()
                        .onSetTileEntity(newPos, newInstance);
            }
            newInstance.markDirty();
        }
    }

    public static void destroyBlockSilent(World world, BlockPos pos) {

    }
}
