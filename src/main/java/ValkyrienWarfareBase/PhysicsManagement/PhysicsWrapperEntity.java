package ValkyrienWarfareBase.PhysicsManagement;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

/**
 * This entity's only purpose is to use the functionality of sending itself
 * to nearby players, all other operations are handled by the PhysicsObject
 * class
 * @author thebest108
 *
 */
public class PhysicsWrapperEntity extends Entity implements IEntityAdditionalSpawnData{

	public PhysicsObject wrapping;
	
	public PhysicsWrapperEntity(World worldIn) {
		super(worldIn);
		wrapping = new PhysicsObject(this);
		if(!worldObj.isRemote){
			
		}
		ValkyrienWarfareMod.physicsManager.onShipLoad(this);
	}
	
	public PhysicsWrapperEntity(World worldIn,double x,double y,double z) {
		this(worldIn);
		posX = x;
		posY = y;
		posZ = z;
		wrapping.claimNewChunks();
		wrapping.processChunkClaims();
	}

	@Override
	public void onUpdate(){
		super.onUpdate();
		wrapping.onTick();
	}
	
	@Override
	protected void entityInit() {
		
	}

	@Override
	public AxisAlignedBB getEntityBoundingBox(){
        return wrapping.collisionBB;
    }
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound tagCompund) {
		wrapping.readFromNBTTag(tagCompund);
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound tagCompound) {
		wrapping.writeToNBTTag(tagCompound);
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		wrapping.preloadNewPlayers();
		wrapping.writeSpawnData(buffer);
	}

	@Override
	public void readSpawnData(ByteBuf additionalData) {
		wrapping.readSpawnData(additionalData);
	}
}
