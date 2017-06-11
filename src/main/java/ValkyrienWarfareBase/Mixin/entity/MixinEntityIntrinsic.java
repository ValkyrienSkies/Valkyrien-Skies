package ValkyrienWarfareBase.Mixin.entity;

import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Collision.EntityCollisionInjector;
import ValkyrienWarfareBase.Collision.EntityCollisionInjector.IntermediateMovementVariableStorage;
import ValkyrienWarfareBase.Interaction.IntrinsicEntityInterface;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(value = Entity.class, priority = 1)
@Implements(@Interface(iface = IntrinsicEntityInterface.class, prefix = "draggable$"))
public abstract class MixinEntityIntrinsic {

    @Shadow
    public double posX;

    @Shadow
    public double posY;

    @Shadow
    public double posZ;

    @Shadow
    public World world;

    public Entity thisClassAsAnEntity = Entity.class.cast(this);

    @Intrinsic(displace = true)
    public void draggable$move(MoverType type, double dx, double dy, double dz){
//    	System.out.println("test");
    	if(PhysicsWrapperEntity.class.isInstance(this)){
    		//Don't move at all
    		return;
    	}

    	double movDistSq = (dx*dx) + (dy*dy) + (dz*dz);

		if(movDistSq > 10000){
			//Assume this will take us to Ship coordinates
			double newX = this.posX + dx;
			double newY = this.posY + dy;
			double newZ = this.posZ + dz;
			BlockPos newPosInBlock = new BlockPos(newX,newY,newZ);
			PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(this.world, newPosInBlock);

			if(wrapper == null){
				return;
			}

			Vector endPos = new Vector(newX,newY,newZ);
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform, endPos);
			dx = endPos.X - this.posX;
			dy = endPos.Y - this.posY;
			dz = endPos.Z - this.posZ;
		}

		IntermediateMovementVariableStorage alteredMovement = EntityCollisionInjector.alterEntityMovement(thisClassAsAnEntity, type, dx, dy, dz);

		if (alteredMovement == null) {
			this.move(type, dx, dy, dz);
		}else{
			this.move(type, alteredMovement.dxyz.X, alteredMovement.dxyz.Y, alteredMovement.dxyz.Z);
			EntityCollisionInjector.alterEntityMovementPost(thisClassAsAnEntity, alteredMovement);
		}
    }

    @Shadow
    public abstract void move(MoverType type, double x, double y, double z);
}
