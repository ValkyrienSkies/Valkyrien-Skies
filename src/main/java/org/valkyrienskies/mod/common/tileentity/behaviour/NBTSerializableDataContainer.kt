package org.valkyrienskies.mod.common.tileentity.behaviour

import com.fasterxml.jackson.databind.ObjectMapper
import net.minecraft.nbt.NBTTagCompound
import org.valkyrienskies.mod.common.util.jackson.VSJacksonUtil

/**
 * Use this class for when you need to serialize data to NBT
 * Primarily intended to be used with TileEntities
 *
 * ```
 * class MyTileEntity : TileEntity() {
 *      val container = NBTSerializableDataContainer(Data(), Data.class)
 *      val data: Data
 *          get() = container.data
 *
 *      override fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
 *          container.writeToNBT(compound)
 *          return super.writeToNBT(compound)
 *      }
 *
 *      override fun readFromNBT(compound: NBTTagCompound) {
 *          super.readFromNBT(compound)
 *          container.readFromNBT(compound)
 *      }
 *
 *      private class Data {
 *          var str = "my stored value"
 *      }
 *  }
 *
 * }
 */
class NBTSerializableDataContainer<DATA>(
        /**
         * Starting/empty data
         */
        data: DATA,
        private val dataClass: Class<DATA>,
        private val serialUID: String = "SerializableTileEntity",
        private val mapper: ObjectMapper = VSJacksonUtil.getDefaultMapper()
) {

    var data = data
        private set

    operator fun component1(): DATA = data

    fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
        val bytes = mapper.writeValueAsBytes(data)
        compound.setByteArray(serialUID, bytes)
        return compound
    }

    fun readFromNBT(compound: NBTTagCompound) {
        val bytes = compound.getByteArray(serialUID)
        data = mapper.readValue(bytes, dataClass)
    }


}