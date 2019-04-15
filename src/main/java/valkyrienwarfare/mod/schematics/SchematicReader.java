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

package valkyrienwarfare.mod.schematics;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

import java.io.InputStream;
import java.util.HashMap;

public class SchematicReader {

    public static HashMap<String, Schematic> schematicCache = new HashMap<String, Schematic>();

    public static Schematic get(String schemname) {
        if (schematicCache.containsKey(schemname)) {
            Schematic tryCached = schematicCache.get(schemname);
            return tryCached;
        }
        try {
            InputStream is = ValkyrienWarfareMod.INSTANCE.getClass().getClassLoader().getResourceAsStream("assets/valkyrienwarfareworld/schematics/" + schemname);
            NBTTagCompound nbtdata = CompressedStreamTools.readCompressed(is);
            short width = nbtdata.getShort("Width");
            short height = nbtdata.getShort("Height");
            short length = nbtdata.getShort("Length");

            byte[] blocks = nbtdata.getByteArray("Blocks");
            byte[] data = nbtdata.getByteArray("Data");
            byte[] addId = new byte[0];

            short[] blocksCombined = new short[blocks.length]; // Have to later combine IDs


            if (nbtdata.getByteArray("AddBlocks") != null) {
                addId = nbtdata.getByteArray("AddBlocks");
            } else {
                System.out.println("fuck");
            }

            // Combine the AddBlocks data with the first 8-bit block ID
            for (int index = 0; index < blocks.length; index++) {
                if ((index >> 1) >= addId.length) { // No corresponding AddBlocks index
                    blocksCombined[index] = (short) (blocks[index] & 0xFF);
                } else {
                    if ((index & 1) == 0) {
                        blocksCombined[index] = (short) (((addId[index >> 1] & 0x0F) << 8) + (blocks[index] & 0xFF));
                    } else {
                        blocksCombined[index] = (short) (((addId[index >> 1] & 0xF0) << 4) + (blocks[index] & 0xFF));
                    }
                }
            }


//            System.out.println("schem size:" + width + " x " + height + " x " + length);
            NBTTagList tileentities = nbtdata.getTagList("TileEntities", 10);
            is.close();

            Schematic toReturn = new Schematic(tileentities, width, height, length, blocks, data, blocksCombined);

            schematicCache.put(schemname, toReturn);

            return toReturn;
        } catch (Exception e) {
            System.out.println("I can't load schematic, because " + e.toString());
            schematicCache.put(schemname, null);
            return null;
        }
    }

    public final static class Schematic {

        public final NBTTagList tileentities;
        public final short width;
        public final short height;
        public final short length;
        public final byte[] blocks;
        public final byte[] data;
        public final short[] blocksCombined;

        private Schematic(NBTTagList tileentities, short width, short height, short length, byte[] blocks, byte[] data, short[] blocksCombined) {
            this.tileentities = tileentities;
            this.width = width;
            this.height = height;
            this.length = length;
            this.blocks = blocks;
            this.data = data;
            this.blocksCombined = blocksCombined;
        }

        public void placeBlockAndTilesInWorld(World worldObj, BlockPos centerDifference) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    for (int z = 0; z < length; z++) {
                        int index = y * width * length + z * width + x;
                        int id = blocksCombined[index];
                        int dataVal = data[index];

                        Block b = Block.getBlockById(id);
                        IBlockState state = b.getStateFromMeta(dataVal);
                        if (state.getBlock() != Blocks.AIR) {
                            worldObj.setBlockState(new BlockPos(x + centerDifference.getX(), y + centerDifference.getY(), z + centerDifference.getZ()), state, 2);
                        }
                    }
                }
            }

            PhysicsWrapperEntity wrapperEntity = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getObjectManagingPos(worldObj, centerDifference);

            for (int i = 0; i < tileentities.tagCount(); i++) {
                NBTTagCompound tileData = tileentities.getCompoundTagAt(i).copy();

                int x = tileData.getInteger("x") + centerDifference.getX();
                int y = tileData.getInteger("y") + centerDifference.getY();
                int z = tileData.getInteger("z") + centerDifference.getZ();

                tileData.setInteger("x", x);
                tileData.setInteger("y", y);
                tileData.setInteger("z", z);

                TileEntity newInstance = TileEntity.create(worldObj, tileData);
                newInstance.validate();

                worldObj.setTileEntity(newInstance.getPos(), newInstance);

                newInstance.markDirty();
            }
        }

    }
}
