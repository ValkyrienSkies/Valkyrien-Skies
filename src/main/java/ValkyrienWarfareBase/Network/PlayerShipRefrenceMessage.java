package ValkyrienWarfareBase.Network;

import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PlayerShipRefrenceMessage implements IMessage {
	
	public Vector playerPosInLocal;
	//    public Vector playeLastPosInLocal;
	public Vector velocityInLocal;
	public Vector playerLookVectorInLocal;
	
	public int shipInID;
	
	public PlayerShipRefrenceMessage() {
	}
	
	public PlayerShipRefrenceMessage(EntityPlayer playerToSend, PhysicsWrapperEntity shipOn) {
		playerPosInLocal = new Vector(playerToSend.posX, playerToSend.posY, playerToSend.posZ);
		velocityInLocal = new Vector(playerToSend.motionX, playerToSend.motionY, playerToSend.motionZ);
		playerLookVectorInLocal = new Vector(playerToSend.getLook(1.0F));
		
		RotationMatrices.applyTransform(shipOn.wrapping.coordTransform.wToLTransform, playerPosInLocal);
		RotationMatrices.doRotationOnly(shipOn.wrapping.coordTransform.wToLRotation, velocityInLocal);
		RotationMatrices.doRotationOnly(shipOn.wrapping.coordTransform.wToLRotation, playerLookVectorInLocal);
		
		shipInID = shipOn.getEntityId();
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		playerPosInLocal = new Vector(buf);
		velocityInLocal = new Vector(buf);
		playerLookVectorInLocal = new Vector(buf);
		shipInID = buf.readInt();
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		playerPosInLocal.writeToByteBuf(buf);
		velocityInLocal.writeToByteBuf(buf);
		playerLookVectorInLocal.writeToByteBuf(buf);
		buf.writeInt(shipInID);
	}
	
}
