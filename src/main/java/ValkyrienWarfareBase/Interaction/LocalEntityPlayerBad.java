package ValkyrienWarfareBase.Interaction;

import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

public class LocalEntityPlayerBad extends EntityPlayerMP {

	EntityPlayerMP realPlayer;
	PhysicsObject parent;

	public LocalEntityPlayerBad(PhysicsObject object, EntityPlayerMP realPlayer) {
		// super(realPlayer.worldObj, realPlayer.getGameProfile());
		super(((WorldServer) realPlayer.worldObj).mcServer, ((WorldServer) realPlayer.worldObj), realPlayer.getGameProfile(), realPlayer.interactionManager);
		this.realPlayer = realPlayer;
		parent = object;
		this.inventoryContainer = realPlayer.inventoryContainer;
		this.inventory = realPlayer.inventory;
		this.openContainer = realPlayer.openContainer;
		this.connection = realPlayer.connection;
		updateVariables();
	}

	public void updateVariables() {
		updatePosition();

	}

	private void updatePosition() {
		posX = realPlayer.posX;
		posY = realPlayer.posY;
		posZ = realPlayer.posZ;
		rotationYawHead = realPlayer.rotationYawHead;
		rotationYaw = realPlayer.rotationYaw;
		rotationPitch = realPlayer.rotationPitch;
		motionX = realPlayer.motionX;
		motionY = realPlayer.motionY;
		motionZ = realPlayer.motionZ;
		RotationMatrices.applyTransform(parent.coordTransform.wToLTransform, parent.coordTransform.wToLRotation, this);
	}

	@Override
	public boolean isSpectator() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCreative() {
		// TODO Auto-generated method stub
		return false;
	}

}
