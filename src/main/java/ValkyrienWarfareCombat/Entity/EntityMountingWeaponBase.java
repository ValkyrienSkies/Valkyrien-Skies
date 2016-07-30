package ValkyrienWarfareCombat.Entity;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

/**
 * Provides a base to easily add more weapons
 *  @author thebest108
 */
public abstract class EntityMountingWeaponBase extends Entity{

	public int delay = 0;
	public double damage = 0;
	
	public EntityMountingWeaponBase(World worldIn) {
		super(worldIn);
	}
	
	@Override
	public boolean processInitialInteract(EntityPlayer player, @Nullable ItemStack stack, EnumHand hand)
    {
		if(player.isRidingSameEntity(this)){
			onRiderInteract(player,stack,hand);
		}
		player.startRiding(this);
        return false;
    }
	
	public abstract void onRiderInteract(EntityPlayer player, @Nullable ItemStack stack, EnumHand hand);
//
//	@Override
//    public boolean interactFirst(EntityPlayer playerIn){
//		if(riddenByEntity!=playerIn){
//			playerIn.mountEntity(this);
//		}else{
//			if(!worldObj.isRemote&&delay==2){
//				Vec3 look = getVectorForRotation(rotationPitch,rotationYaw);
//				if(riddenByEntity != null){
//					look = riddenByEntity.getLookVec();
//				}
//				ContactTNT tnt = new ContactTNT(worldObj);
//				
//				double speed = 2.5D;
//				
//				tnt.setPosition(posX+look.xCoord, posY+.6+look.yCoord, posZ+look.zCoord);
//				tnt.motionX = look.xCoord*speed;
//				tnt.motionY = look.yCoord*speed;
//				tnt.motionZ = look.zCoord*speed;
//				worldObj.spawnEntityInWorld(tnt);
//				worldObj.playSoundAtEntity(tnt, "game.tnt.primed", 1.0F, 1.0F);
//				delay=0;
//			}else{
//				delay++;
//			}
//		}
//		return true;
//	}

	@Override
    public void updateRidden(){
		System.out.println("test");
	}

//	@Override
//	public void updateRiderPosition(){
//		if(riddenByEntity != null){
//			riddenByEntity.setPosition(posX, posY + getMountedYOffset() + riddenByEntity.getYOffset(), posZ);
//			if(!ValkyrianWarfareMod.entityFixingManager.isEntityFixed(this)){
//	            rotationYaw = riddenByEntity.getRotationYawHead();
//	            rotationPitch = riddenByEntity.rotationPitch;
//			}else{
//				Ship fixedOn = ValkyrianWarfareMod.entityFixingManager.getShipForFixedEntity(this);
//				if(fixedOn!=null){
//					Vec3 pointingDirection = riddenByEntity.getLook(1.0F);
//					pointingDirection = RotationMatrices.applyTransform(RotationMatrices.inverse(fixedOn.rotationTransform), pointingDirection);
//					double newPitch = Math.asin(pointingDirection.yCoord)* -180f/Math.PI;
//					float f4 = (float) -Math.cos(-newPitch * 0.017453292F);
//					double radianYaw = Math.atan2((pointingDirection.xCoord/f4), (pointingDirection.zCoord/f4));
//					radianYaw+=Math.PI;
//					radianYaw*= -180f/Math.PI;
//					rotationYaw = (float) radianYaw;
//					rotationPitch = (float) newPitch;
//				}	
//			} 
//		}
//	}

	@Override
    public double getMountedYOffset(){
        return -.1D;
    }

	@Override
    public boolean canBeCollidedWith(){
        return !isDead;
    }

	@Override
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int rotationSteps, boolean p_180426_10_){
		if(p_180426_10_){
			posX=x;
			posY=y;
			posZ=z;
			rotationYaw=yaw;
			rotationPitch=pitch;
		}
	}

	@Override
	public void onUpdate(){
		super.onUpdate();
		
		Entity rider = getRider();
		if(rider!=null){
			rotationYaw = rider.getRotationYawHead();
			rotationPitch = rider.rotationPitch;
		}
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

	@Nullable
	public Entity getRider(){
		Entity entity = this.getPassengers().isEmpty() ? null : (Entity)this.getPassengers().get(0);
		return entity;
	}
	
	@Override
	public boolean canRiderInteract(){
        return true;
    }

	@Override
	protected void entityInit() {
		
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (!this.worldObj.isRemote && !this.isDead)
        {
            if (this.isEntityInvulnerable(source))
            {
                return false;
            }
            else
            {
                this.setBeenAttacked();
                this.setDamage(this.getDamage() + amount * 10.0F);
                boolean flag = source.getEntity() instanceof EntityPlayer && ((EntityPlayer)source.getEntity()).capabilities.isCreativeMode;

                if (flag || this.getDamage() > getMaxDamage())
                {
                    this.removePassengers();

                    if (flag && !this.hasCustomName())
                    {
                        this.setDead();
                    }
                    else
                    {
                        this.killWeapon(source);
                    }
                }

                return true;
            }
        }
        else
        {
            return true;
        }
    }

	public void setDamage(double toSet){
		damage = toSet;
	}
	
	public double getDamage(){
		return damage;
	}
	
	public double getMaxDamage(){
		return 50D;
	}
	
	public void killWeapon(DamageSource source)
    {
        this.setDead();
        if (this.worldObj.getGameRules().getBoolean("doEntityDrops"))
        {
        	doItemDrops();
        }
    }
	
	public void doItemDrops(){
//      ItemStack itemstack = new ItemStack(Items.MINECART, 1);
//
//      if (this.getName() != null)
//      {
//          itemstack.setStackDisplayName(this.getName());
//      }
//
//      this.entityDropItem(itemstack, 0.0F);
	}
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound tagCompund) {
		
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound tagCompound) {
		
	}

}