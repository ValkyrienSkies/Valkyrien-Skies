package ValkyrienWarfareCombat.Entity;

import ValkyrienWarfareBase.API.RotationMatrices;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class BasicCannonEntity extends Entity{

	public int delay = 0;

	public BasicCannonEntity(World worldIn) {
		super(worldIn);
	}

	@Override
    public double getMountedYOffset(){
        return .5D;
    }

	@Override
    public boolean canBeCollidedWith(){
        return !isDead;
    }

	@Override
	public void onUpdate(){
		super.onUpdate();
	}

	@Override
	public void moveEntity(double x, double y, double z){
		super.moveEntity(x, y, z);
    }

	@Override
    public AxisAlignedBB getEntityBoundingBox(){
        return boundingBox;
    }

	@Override
    public AxisAlignedBB getCollisionBox(Entity entityIn){
        return boundingBox;
    }

	@Override
	public boolean canRiderInteract(){
        return true;
    }

	@Override
	protected void entityInit() {
		
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound tagCompund) {
		
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound tagCompound) {
		
	}

}