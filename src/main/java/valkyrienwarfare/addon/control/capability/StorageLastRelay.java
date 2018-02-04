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

package valkyrienwarfare.addon.control.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class StorageLastRelay implements IStorage<ICapabilityLastRelay> {

    @Override
    public NBTBase writeNBT(Capability<ICapabilityLastRelay> capability, ICapabilityLastRelay instance, EnumFacing side) {
        int x = 0, y = 0, z = 0;
        
        if (instance.hasLastRelay()) {
            x = instance.getLastRelay().getX();
            y = instance.getLastRelay().getY();
            z = instance.getLastRelay().getZ();
        }
        
        return new NBTTagIntArray(new int[]{x, y, z});
    }

    @Override
    public void readNBT(Capability<ICapabilityLastRelay> capability, ICapabilityLastRelay instance, EnumFacing side, NBTBase nbt) {
        NBTTagIntArray tag = (NBTTagIntArray) nbt;
        int[] backingArray = tag.getIntArray();
        //If all these values are 0, then assume the blockPos was just null anyways
        if (!(backingArray[0] == 0 && backingArray[1] == 0 && backingArray[2] == 0)) {
            BlockPos pos = new BlockPos(backingArray[0], backingArray[1], backingArray[2]);
            instance.setLastRelay(pos);
        }
    }

}
