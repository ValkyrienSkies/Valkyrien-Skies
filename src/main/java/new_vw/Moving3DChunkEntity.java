package new_vw;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class Moving3DChunkEntity extends Entity {

	int vw_chunkX, vw_chunkZ;
	int vw_minChunkY, vw_maxChunkY;
	Moving3DChunk vw_myChunk;
	
	public Moving3DChunkEntity(World worldIn, int chunkX, int chunkZ, int minChunkY, int maxChunkY) {
		super(worldIn);
	}
	
	public Moving3DChunkEntity(World worldIn) {
		super(worldIn);
	}

	@Override
	protected void entityInit() {
		// TODO Auto-generated method stub
		
	}

	protected Moving3DChunk loadChunk() {
		Moving3DChunk theChunk = new Moving3DChunk(this);
		vw_myChunk = theChunk;
		return theChunk;
	}
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		int[] internalVars = compound.getIntArray("internalVars");
		
		vw_chunkX = internalVars[0];
		vw_chunkZ = internalVars[1];
		vw_minChunkY = internalVars[2];
		vw_minChunkY = internalVars[3];
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		compound.setIntArray("internalVars", new int[]{vw_chunkX, vw_chunkZ, vw_minChunkY, vw_maxChunkY});
		
	}

}
