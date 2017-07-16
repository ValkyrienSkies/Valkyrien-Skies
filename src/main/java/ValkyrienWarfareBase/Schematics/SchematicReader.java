package ValkyrienWarfareBase.Schematics;

import java.io.InputStream;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class SchematicReader {

	public static Schematic get(String schemname){
        try {
            InputStream is = ValkyrienWarfareMod.instance.getClass().getClassLoader().getResourceAsStream("assets/valkyrienwarfare/schematics/"+schemname);
            NBTTagCompound nbtdata = CompressedStreamTools.readCompressed(is);
            short width = nbtdata.getShort("Width");
            short height = nbtdata.getShort("Height");
            short length = nbtdata.getShort("Length");

            byte[] blocks = nbtdata.getByteArray("Blocks");
            byte[] data = nbtdata.getByteArray("Data");
            byte[] addId = new byte[0];

            short[] blocksCombined = new short[blocks.length]; // Have to later combine IDs


            if(nbtdata.getByteArray("AddBlocks") != null) {
            	addId = nbtdata.getByteArray("AddBlocks");
            }else{
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

            return new Schematic(tileentities, width, height, length, blocks, data, blocksCombined);
        } catch (Exception e) {
            System.out.println("I can't load schematic, because " + e.toString());
            return null;
        }
    }

    public final static class Schematic{
        public final NBTTagList tileentities;
        public final short width;
        public final short height;
        public final short length;
        public final byte[] blocks;
        public final byte[] data;
        public final short[] blocksCombined;

        public Schematic(NBTTagList tileentities, short width, short height, short length, byte[] blocks, byte[] data, short[] blocksCombined){
            this.tileentities = tileentities;
            this.width = width;
            this.height = height;
            this.length = length;
            this.blocks = blocks;
            this.data = data;
            this.blocksCombined = blocksCombined;
        }

    }
}
