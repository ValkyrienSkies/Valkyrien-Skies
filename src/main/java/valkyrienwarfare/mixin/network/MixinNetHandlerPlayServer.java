/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.mixin.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import valkyrienwarfare.mod.physmanagement.interaction.EntityDraggable;
import valkyrienwarfare.mod.physmanagement.interaction.IDraggable;
import valkyrienwarfare.mod.physmanagement.interaction.INHPServerVW;

//TODO: a lot of these mixins can probably be done using overrides instead of overwrites, i should have a look at some point
@Mixin(value = NetHandlerPlayServer.class, priority = 5)
public abstract class MixinNetHandlerPlayServer implements INHPServerVW {

    @Shadow
    @Final
    public static Logger LOGGER;
    @Shadow
    public EntityPlayerMP player;
    @Shadow
    @Final
    public MinecraftServer serverController;
    @Shadow
    public int networkTickCount;
    @Shadow
    public Vec3d targetPos;
    @Shadow
    public int lastPositionUpdate;
    @Shadow
    public double firstGoodX;
    @Shadow
    public double firstGoodY;
    @Shadow
    public double firstGoodZ;
    @Shadow
    public double lastGoodX;
    @Shadow
    public double lastGoodY;
    @Shadow
    public double lastGoodZ;
    @Shadow
    public int movePacketCounter;
    @Shadow
    public int lastMovePacketCounter;
    @Shadow
    public boolean floating;

    private double dummyBlockReachDist = 9999999999999999999999999999D;
    private double lastGoodBlockReachDist;
    // Thanks java
    private NetHandlerPlayServer thisAsNetHandler = NetHandlerPlayServer.class.cast(this);

    @Shadow
    private static boolean isMovePlayerPacketInvalid(CPacketPlayer packetIn) {
        return false;
    }

    @Redirect(method = "update",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/NetHandlerPlayServer;captureCurrentPosition()V"))
    public void makeIDraggableNotUseless(NetHandlerPlayServer server) {
        IDraggable draggable = EntityDraggable.getDraggableFromEntity(player);
        server.captureCurrentPosition();

        server.firstGoodX += draggable.getVelocityAddedToPlayer().X;
        server.firstGoodY += draggable.getVelocityAddedToPlayer().Y;
        server.firstGoodZ += draggable.getVelocityAddedToPlayer().Z;
        server.lastGoodX += draggable.getVelocityAddedToPlayer().X;
        server.lastGoodY += draggable.getVelocityAddedToPlayer().Y;
        server.lastGoodZ += draggable.getVelocityAddedToPlayer().Z;
    }

    @Override
    public double dummyBlockReachDist() {
        return dummyBlockReachDist;
    }

    @Override
    public void dummyBlockReachDist(double in) {
        dummyBlockReachDist = in;
    }

    @Override
    public double lastGoodBlockReachDist() {
        return lastGoodBlockReachDist;
    }

    @Override
    public void lastGoodBlockReachDist(double in) {
        lastGoodBlockReachDist = in;
    }

    @Override
    public void checkForPacketEnqueueTrap(Packet packetIn) {
        // Thanks sponge for breaking it
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, NetHandlerPlayServer.class.cast(this), this.player.getServerWorld());
        // SpongeStrangenessFixes.checkThreadAndEnqueue_SpongeFree(packetIn, NetHandlerPlayServer.class.cast(this), player.getServerWorld());
    }

    @Override
    public EntityPlayerMP getEntityPlayerFromHandler() {
        return player;
    }

    @Shadow
    public void disconnect(final ITextComponent textComponent) {
    }

    @Shadow
    public void captureCurrentPosition() {
    }

    @Shadow
    public void setPlayerLocation(double x, double y, double z, float yaw, float pitch) {
    }

    // Because of MCs bad coding I have to copy an entire method just to edit one line of code.
    /*
    @Overwrite
    public void processPlayer(CPacketPlayer packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, thisAsNetHandler, this.player.getServerWorld());

        if (isMovePlayerPacketInvalid(packetIn))
        {
            this.disconnect(new TextComponentTranslation("multiplayer.disconnect.invalid_player_movement", new Object[0]));
        }
        else
        {
            WorldServer worldserver = this.serverController.getWorld(this.player.dimension);

            if (!this.player.queuedEndExit)
            {
                if (this.networkTickCount == 0)
                {
                    this.captureCurrentPosition();
                }

                if (this.targetPos != null)
                {
                    if (this.networkTickCount - this.lastPositionUpdate > 20)
                    {
                        this.lastPositionUpdate = this.networkTickCount;
                        this.setPlayerLocation(this.targetPos.x, this.targetPos.y, this.targetPos.z, this.player.rotationYaw, this.player.rotationPitch);
                    }
                }
                else
                {
                    this.lastPositionUpdate = this.networkTickCount;

                    if (this.player.isRiding())
                    {
                        this.player.setPositionAndRotation(this.player.posX, this.player.posY, this.player.posZ, packetIn.getYaw(this.player.rotationYaw), packetIn.getPitch(this.player.rotationPitch));
                        this.serverController.getPlayerList().serverUpdateMovingPlayer(this.player);
                    }
                    else
                    {
                        double d0 = this.player.posX;
                        double d1 = this.player.posY;
                        double d2 = this.player.posZ;
                        double d3 = this.player.posY;
                        double d4 = packetIn.getX(this.player.posX);
                        double d5 = packetIn.getY(this.player.posY);
                        double d6 = packetIn.getZ(this.player.posZ);
                        float f = packetIn.getYaw(this.player.rotationYaw);
                        float f1 = packetIn.getPitch(this.player.rotationPitch);
                        double d7 = d4 - this.firstGoodX;
                        double d8 = d5 - this.firstGoodY;
                        double d9 = d6 - this.firstGoodZ;
                        double d10 = this.player.motionX * this.player.motionX + this.player.motionY * this.player.motionY + this.player.motionZ * this.player.motionZ;
                        double d11 = d7 * d7 + d8 * d8 + d9 * d9;

                        if (this.player.isPlayerSleeping())
                        {
                            if (d11 > 1.0D)
                            {
                                this.setPlayerLocation(this.player.posX, this.player.posY, this.player.posZ, packetIn.getYaw(this.player.rotationYaw), packetIn.getPitch(this.player.rotationPitch));
                            }
                        }
                        else
                        {
                            ++this.movePacketCounter;
                            int i = this.movePacketCounter - this.lastMovePacketCounter;

                            if (i > 5)
                            {
                                LOGGER.debug("{} is sending move packets too frequently ({} packets since last tick)", this.player.getName(), Integer.valueOf(i));
                                i = 1;
                            }

                            if (!this.player.isInvulnerableDimensionChange() && (!this.player.getServerWorld().getGameRules().getBoolean("disableElytraMovementCheck") || !this.player.isElytraFlying()))
                            {
                                float f2 = this.player.isElytraFlying() ? 300.0F : 100.0F;

                                if (d11 - d10 > f2 * i && (!this.serverController.isSinglePlayer() || !this.serverController.getServerOwner().equals(this.player.getName())))
                                {
                                    LOGGER.warn("{} moved too quickly! {},{},{}", this.player.getName(), Double.valueOf(d7), Double.valueOf(d8), Double.valueOf(d9));
                                    this.setPlayerLocation(this.player.posX, this.player.posY, this.player.posZ, this.player.rotationYaw, this.player.rotationPitch);
                                    return;
                                }
                            }

                            boolean flag2 = worldserver.getCollisionBoxes(this.player, this.player.getEntityBoundingBox().shrink(0.0625D)).isEmpty();
                            d7 = d4 - this.lastGoodX;
                            d8 = d5 - this.lastGoodY;
                            d9 = d6 - this.lastGoodZ;

                            if (this.player.onGround && !packetIn.isOnGround() && d8 > 0.0D)
                            {
                                this.player.jump();
                            }

                            this.player.move(MoverType.PLAYER, d7, d8, d9);
                            this.player.onGround = packetIn.isOnGround();
                            double d12 = d8;
                            d7 = d4 - this.player.posX;
                            d8 = d5 - this.player.posY;

                            if (d8 > -0.5D || d8 < 0.5D)
                            {
                                d8 = 0.0D;
                            }

                            d9 = d6 - this.player.posZ;
                            d11 = d7 * d7 + d8 * d8 + d9 * d9;
                            boolean flag = false;

                            if (false && !this.player.isInvulnerableDimensionChange() && d11 > 0.0625D && !this.player.isPlayerSleeping() && !this.player.interactionManager.isCreative() && this.player.interactionManager.getGameType() != GameType.SPECTATOR)
                            {
                                flag = true;
                                LOGGER.warn("{} moved wrongly!", this.player.getName());
                            }

                            this.player.setPositionAndRotation(d4, d5, d6, f, f1);
                            this.player.addMovementStat(this.player.posX - d0, this.player.posY - d1, this.player.posZ - d2);

                            if (!this.player.noClip && !this.player.isPlayerSleeping())
                            {
                                boolean flag1 = worldserver.getCollisionBoxes(this.player, this.player.getEntityBoundingBox().shrink(0.0625D)).isEmpty();

                                if (flag2 && (flag || !flag1))
                                {
                                    this.setPlayerLocation(d0, d1, d2, f, f1);
                                    return;
                                }
                            }

                            this.floating = d12 >= -0.03125D;
                            this.floating &= !this.serverController.isFlightAllowed() && !this.player.capabilities.allowFlying;
                            this.floating &= !this.player.isPotionActive(MobEffects.LEVITATION) && !this.player.isElytraFlying() && !worldserver.checkBlockCollision(this.player.getEntityBoundingBox().grow(0.0625D).expand(0.0D, -0.55D, 0.0D));
                            this.player.onGround = packetIn.isOnGround();
                            this.serverController.getPlayerList().serverUpdateMovingPlayer(this.player);
                            this.player.handleFalling(this.player.posY - d3, packetIn.isOnGround());
                            this.lastGoodX = this.player.posX;
                            this.lastGoodY = this.player.posY;
                            this.lastGoodZ = this.player.posZ;
                        }
                    }
                }
            }
        }
    }
    */

}
