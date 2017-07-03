package ValkyrienWarfareBase.Interaction;

import net.minecraft.entity.player.EntityPlayer;

public class PlayerDataBackup {

	private final EntityPlayer playerToBackup;
	private double posX, posY, posZ;
	private double lastTickPosX, lastTickPosY, lastTickPosZ;
	private float rotationYaw, rotationPitch;
	private float prevRotationYaw, prevRotationPitch;
	private double motionX, motionY, motionZ;

	public PlayerDataBackup(EntityPlayer playerToBackup) {
		this.playerToBackup = playerToBackup;
		generatePlayerBackup();
	}

	public void generatePlayerBackup() {
		posX = playerToBackup.posX;
		lastTickPosX = playerToBackup.lastTickPosX;
		posY = playerToBackup.posY;
		lastTickPosY = playerToBackup.lastTickPosY;
		posZ = playerToBackup.posZ;
		lastTickPosZ = playerToBackup.lastTickPosZ;
		rotationYaw = playerToBackup.rotationYaw;
		rotationPitch = playerToBackup.rotationPitch;
		prevRotationYaw = playerToBackup.prevRotationYaw;
		prevRotationPitch = playerToBackup.prevRotationPitch;
		motionX = playerToBackup.motionX;
		motionY = playerToBackup.motionY;
		motionZ = playerToBackup.motionZ;
	}

	public void restorePlayerToBackup() {
		playerToBackup.posX = posX;
		playerToBackup.lastTickPosX = lastTickPosX;
		playerToBackup.posY = posY;
		playerToBackup.lastTickPosY = lastTickPosY;
		playerToBackup.posZ = posZ;
		playerToBackup.lastTickPosZ = lastTickPosZ;
		playerToBackup.rotationYaw = rotationYaw;
		playerToBackup.rotationPitch = rotationPitch;
		playerToBackup.prevRotationYaw = prevRotationYaw;
		playerToBackup.prevRotationPitch = prevRotationPitch;
		playerToBackup.setPosition(posX, posY, posZ);
		playerToBackup.motionX = motionX;
		playerToBackup.motionY = motionY;
		playerToBackup.motionZ = motionZ;
	}
}
