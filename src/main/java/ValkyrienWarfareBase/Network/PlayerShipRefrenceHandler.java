package ValkyrienWarfareBase.Network;

import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PlayerShipRefrenceHandler implements IMessageHandler<PlayerShipRefrenceMessage, IMessage> {

    @Override
    public IMessage onMessage(PlayerShipRefrenceMessage message, MessageContext ctx) {
        //This seems to be being called on the server!!!
        IThreadListener mainThread = ctx.getServerHandler().serverController;
		mainThread.addScheduledTask(new Runnable() {
			@Override
			public void run() {
				EntityPlayerMP playerEntity = ctx.getServerHandler().player;
				PhysicsWrapperEntity wrapper = (PhysicsWrapperEntity) playerEntity.world.getEntityByID(message.shipInID);

				if(wrapper != null) {
					double[] lToWTransform = wrapper.wrapping.coordTransform.lToWTransform;
					double[] lToWRotation = wrapper.wrapping.coordTransform.lToWRotation;

					Vector newPlayerPos = new Vector(message.playerPosInLocal);
					Vector newPlayerVelocity = new Vector(message.velocityInLocal);
					Vector newPlayerLook = new Vector(message.playerLookVectorInLocal);

					newPlayerPos.transform(lToWTransform);
					newPlayerVelocity.transform(lToWRotation);
					newPlayerLook.transform(lToWRotation);

					playerEntity.posX = newPlayerPos.X;
					playerEntity.posY = newPlayerPos.Y;
					playerEntity.posZ = newPlayerPos.Z;

					playerEntity.motionX = newPlayerVelocity.X;
					playerEntity.motionY = newPlayerVelocity.Y;
					playerEntity.motionZ = newPlayerVelocity.Z;

					double newPitch = Math.asin(newPlayerLook.Y) * -180D / Math.PI;
		            double f4 = -Math.cos(-newPitch * 0.017453292D);
		            double radianYaw = Math.atan2((newPlayerLook.X / f4), (newPlayerLook.Z / f4));
		            radianYaw += Math.PI;
		            radianYaw *= -180D / Math.PI;

		            if (!(Double.isNaN(radianYaw) || Math.abs(newPitch) > 85)) {
		                double wrappedYaw = MathHelper.wrapDegrees(radianYaw);
		                double wrappedRotYaw = MathHelper.wrapDegrees(playerEntity.rotationYaw);
		                double yawDif = wrappedYaw - wrappedRotYaw;
		                if (Math.abs(yawDif) > 180D) {
		                    if (yawDif < 0) {
		                        yawDif += 360D;
		                    } else {
		                        yawDif -= 360D;
		                    }
		                }
		                yawDif %= 360D;
		                final double threshold = .1D;
		                if (Math.abs(yawDif) < threshold) {
		                    yawDif = 0D;
		                }
		                playerEntity.rotationYaw += yawDif;
		            }
				}
			}
		});
        return null;
    }

}
