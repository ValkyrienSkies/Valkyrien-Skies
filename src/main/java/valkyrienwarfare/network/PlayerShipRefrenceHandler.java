/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2017 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package valkyrienwarfare.network;

import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;
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
				
				if (wrapper != null) {
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
