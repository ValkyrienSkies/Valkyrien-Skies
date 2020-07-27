package org.valkyrienskies.mod.common.ships.block_relocation;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.valkyrienskies.mod.common.physics.BlockPhysicsDetails;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;

import java.util.Optional;

public class MoveBlocks {

    /**
     * @param physicsObjectOptional Used when we're using this to copy from world to physics object;
     *                              should be empty when other way around.
     */
    public static void copyBlockToPos(World world, BlockPos oldPos, BlockPos newPos,
        Optional<PhysicsObject> physicsObjectOptional) {
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
            chunkToSet.storageArrays[storageIndex] = new ExtendedBlockStorage(storageIndex << 4,
                true);
        }
        chunkToSet.storageArrays[storageIndex]
            .set(newPos.getX() & 15, newPos.getY() & 15, newPos.getZ() & 15, newState);
        // Only want to send the update to clients and nothing else, so we use flag 2.
        world.notifyBlockUpdate(newPos, oldState, newState, 2);

        // If this block is force block, then add it to the activeForcePositions list of the ship.
        if (physicsObjectOptional.isPresent() && BlockPhysicsDetails.isBlockProvidingForce(newState)) {
            physicsObjectOptional.get().getShipData().activeForcePositions.add(newPos);
        }

        // Now that we've copied the block to the position, copy the tile entity
        copyTileEntityToPos(world, oldPos, newPos, physicsObjectOptional);
    }

    private static void copyTileEntityToPos(World world, BlockPos oldPos, BlockPos newPos,
        Optional<PhysicsObject> physicsObjectOptional) {
        // Make a copy of the tile entity at oldPos to newPos
        TileEntity worldTile = world.getTileEntity(oldPos);
        if (worldTile != null) {
            NBTTagCompound tileEntNBT = new NBTTagCompound();
            TileEntity newInstance;
            if (worldTile instanceof IRelocationAwareTile) {
                newInstance = ((IRelocationAwareTile) worldTile).createRelocatedTile(newPos,
                    physicsObjectOptional.map(PhysicsObject::getShipData).orElse(null));
            } else {
                tileEntNBT = worldTile.writeToNBT(tileEntNBT);
                // Change the block position to be inside of the Ship
                tileEntNBT.setInteger("x", newPos.getX());
                tileEntNBT.setInteger("y", newPos.getY());
                tileEntNBT.setInteger("z", newPos.getZ());
                newInstance = TileEntity.create(world, tileEntNBT);
            }
            // Do post relocation behavior
            if (newInstance instanceof IPostRelocationAwareTile) {
                if (physicsObjectOptional.isPresent()) {
                    ((IPostRelocationAwareTile) newInstance).postRelocation(newPos, oldPos, physicsObjectOptional.get());
                } else {
                    ((IPostRelocationAwareTile) newInstance).postRelocation(newPos, oldPos, null);
                }
            }

            try {
                world.setTileEntity(newPos, newInstance);
                physicsObjectOptional
                    .ifPresent(physicsObject -> physicsObject.onSetTileEntity(newPos, newInstance));
                newInstance.markDirty();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
