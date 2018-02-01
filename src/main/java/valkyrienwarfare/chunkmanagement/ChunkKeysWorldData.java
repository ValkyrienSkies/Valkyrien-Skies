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

package valkyrienwarfare.chunkmanagement;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.ArrayList;

public class ChunkKeysWorldData extends WorldSavedData {

    private static final String key = "ChunkKeys";
    public ArrayList<Integer> avalibleChunkKeys = new ArrayList<Integer>();
    public int chunkKey;

    public ChunkKeysWorldData() {
        super(key);
    }

    public ChunkKeysWorldData(String name) {
        super(name);
    }

    public static ChunkKeysWorldData get(World world) {
        MapStorage storage = world.getPerWorldStorage();
        ChunkKeysWorldData data = (ChunkKeysWorldData) storage.getOrLoadData(ChunkKeysWorldData.class, key);
        if (data == null) {
            data = new ChunkKeysWorldData();
            world.setData(key, data);
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        chunkKey = nbt.getInteger("chunkKey");
        int[] array = nbt.getIntArray("avalibleChunkKeys");
        for (int i : array) {
            avalibleChunkKeys.add(i);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("chunkKey", chunkKey);
        int[] array = new int[avalibleChunkKeys.size()];
        for (int i = 0; i < avalibleChunkKeys.size(); i++) {
            array[i] = avalibleChunkKeys.get(i);
        }
        nbt.setIntArray("avalibleChunkKeys", array);
        return nbt;
    }

}
