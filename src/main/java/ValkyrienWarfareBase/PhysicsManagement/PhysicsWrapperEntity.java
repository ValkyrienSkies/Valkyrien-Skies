package ValkyrienWarfareBase.PhysicsManagement;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * This entity's only purpose is to use the functionality of sending itself
 * to nearby players, all other operations are handled by the PhysicsObject
 * class
 * @author thebest108
 *
 */
public class PhysicsWrapperEntity extends Entity{

	public PhysicsObject wrapping;
	public double pitch,yaw,roll;
	
	public PhysicsWrapperEntity(World worldIn) {
		super(worldIn);
		wrapping = new PhysicsObject(this);
	}
	
	public PhysicsWrapperEntity(World worldIn,double x,double y,double z) {
		this(worldIn);
		posX = x;
		posY = y;
		posZ = z;
		wrapping.generateNewChunks();
	}

	@Override
	protected void entityInit() {
		
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound tagCompund) {
		wrapping.readFromNBTTag(tagCompund);
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound tagCompound) {
		wrapping.writeToNBTTag(tagCompound);
	}
}
