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


            System.out.println("schem size:" + width + " x " + height + " x " + length);
            NBTTagList tileentities = nbtdata.getTagList("TileEntities", 10);
            is.close();

            return new Schematic(tileentities, width, height, length, blocks, data);
        } catch (Exception e) {
            System.out.println("I can't load schematic, because " + e.toString());
            return null;
        }
    }

    public static class Schematic{
        public NBTTagList tileentities;
        public short width;
        public short height;
        public short length;
        public byte[] blocks;
        public byte[] data;
        public Schematic(NBTTagList tileentities, short width, short height, short length, byte[] blocks, byte[] data){
            this.tileentities = tileentities;
            this.width = width;
            this.height = height;
            this.length = length;
            this.blocks = blocks;
            this.data = data;
        }

    }
}
