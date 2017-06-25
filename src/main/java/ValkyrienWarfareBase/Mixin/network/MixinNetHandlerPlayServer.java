package ValkyrienWarfareBase.Mixin.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;

@Mixin(NetHandlerPlayServer.class)
public abstract class MixinNetHandlerPlayServer {

    public NetHandlerPlayServer thisClassAsAHandler = NetHandlerPlayServer.class.cast(this);
    @Shadow
    public EntityPlayerMP player;
    private double dummyBlockReachDist = 9999999999999999999999999999D;
    private double lastGoodBlockReachDist;
    private int ticksSinceLastTry = 0;

    @Overwrite
    public void processTryUseItemOnBlock(CPacketPlayerTryUseItemOnBlock packetIn) {
        BlockPos packetPos = packetIn.getPos();
        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(player.world, packetPos);
        if (player.interactionManager.getBlockReachDistance() != dummyBlockReachDist) {
            lastGoodBlockReachDist = player.interactionManager.getBlockReachDistance();
        }
        if (wrapper != null) {
            player.interactionManager.setBlockReachDistance(dummyBlockReachDist);
            ticksSinceLastTry = 0;
        }

        PacketThreadUtil.checkThreadAndEnqueue(packetIn, NetHandlerPlayServer.class.cast(this), this.player.getServerWorld());
        if (wrapper != null && wrapper.wrapping.coordTransform != null) {
            float playerYaw = player.rotationYaw;
            float playerPitch = player.rotationPitch;
            RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform, wrapper.wrapping.coordTransform.wToLRotation, player);
            if (player.getHeldItem(packetIn.getHand()) != null && player.getHeldItem(packetIn.getHand()).getItem() instanceof ItemBucket) {
                player.interactionManager.setBlockReachDistance(lastGoodBlockReachDist);
            }
            try {
                processTryUseItemOnBlockOriginal(packetIn);
            } catch (Exception e) {
            }
            RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, wrapper.wrapping.coordTransform.lToWRotation, player);
            player.rotationYaw = playerYaw;
            player.rotationPitch = playerPitch;
        } else {
            processTryUseItemOnBlockOriginal(packetIn);
        }
        player.interactionManager.setBlockReachDistance(lastGoodBlockReachDist);
    }

    @Overwrite
    public void processPlayerDigging(CPacketPlayerDigging packetIn) {
        BlockPos packetPos = packetIn.getPosition();
        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(player.world, packetPos);
        if (player.interactionManager.getBlockReachDistance() != dummyBlockReachDist) {
            lastGoodBlockReachDist = player.interactionManager.getBlockReachDistance();
        }
        if (wrapper != null) {
            player.interactionManager.setBlockReachDistance(dummyBlockReachDist);
            ticksSinceLastTry = 0;
        }

        PacketThreadUtil.checkThreadAndEnqueue(packetIn, NetHandlerPlayServer.class.cast(this), this.player.getServerWorld());
        if (wrapper != null && wrapper.wrapping.coordTransform != null) {
            float playerYaw = player.rotationYaw;
            float playerPitch = player.rotationPitch;
            RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform, wrapper.wrapping.coordTransform.wToLRotation, player);
            processPlayerDiggingOriginal(packetIn);
            RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, wrapper.wrapping.coordTransform.lToWRotation, player);
            player.rotationYaw = playerYaw;
            player.rotationPitch = playerPitch;
        } else {
            processPlayerDiggingOriginal(packetIn);
        }
        player.interactionManager.setBlockReachDistance(lastGoodBlockReachDist);
    }

    @Overwrite
    public void processUpdateSign(CPacketUpdateSign packetIn) {
        BlockPos packetPos = packetIn.getPosition();
        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(player.world, packetPos);
        if (player.interactionManager.getBlockReachDistance() != dummyBlockReachDist) {
            lastGoodBlockReachDist = player.interactionManager.getBlockReachDistance();
        }
        if (wrapper != null) {
            player.interactionManager.setBlockReachDistance(dummyBlockReachDist);
            ticksSinceLastTry = 0;
        }

        PacketThreadUtil.checkThreadAndEnqueue(packetIn, NetHandlerPlayServer.class.cast(this), this.player.getServerWorld());
        if (wrapper != null && wrapper.wrapping.coordTransform != null) {
            float playerYaw = player.rotationYaw;
            float playerPitch = player.rotationPitch;
            RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform, wrapper.wrapping.coordTransform.wToLRotation, player);
            processUpdateSignOriginal(packetIn);
            RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, wrapper.wrapping.coordTransform.lToWRotation, player);
            player.rotationYaw = playerYaw;
            player.rotationPitch = playerPitch;
        } else {
            processUpdateSignOriginal(packetIn);
        }
        player.interactionManager.setBlockReachDistance(lastGoodBlockReachDist);
    }

    public void processTryUseItemOnBlockOriginal(CPacketPlayerTryUseItemOnBlock packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, thisClassAsAHandler, this.player.getServerWorld());
        WorldServer worldserver = thisClassAsAHandler.serverController.worldServerForDimension(this.player.dimension);
        EnumHand enumhand = packetIn.getHand();
        ItemStack itemstack = this.player.getHeldItem(enumhand);
        BlockPos blockpos = packetIn.getPos();
        EnumFacing enumfacing = packetIn.getDirection();
        this.player.markPlayerActive();

        if (blockpos.getY() < thisClassAsAHandler.serverController.getBuildLimit() - 1 || enumfacing != EnumFacing.UP && blockpos.getY() < thisClassAsAHandler.serverController.getBuildLimit()) {
            double dist = player.interactionManager.getBlockReachDistance() + 3;
            dist *= dist;
            if (thisClassAsAHandler.targetPos == null && this.player.getDistanceSq((double) blockpos.getX() + 0.5D, (double) blockpos.getY() + 0.5D, (double) blockpos.getZ() + 0.5D) < dist && !thisClassAsAHandler.serverController.isBlockProtected(worldserver, blockpos, this.player) && worldserver.getWorldBorder().contains(blockpos)) {
                Vector playerHitVec = new Vector(packetIn.getFacingX(), packetIn.getFacingY(), packetIn.getFacingZ());

                Vector distanceVector = new Vector(playerHitVec);
                distanceVector.subtract(blockpos.getX(), blockpos.getY(), blockpos.getZ());
                if(distanceVector.lengthSq() > 64) {
                	//Move it back to local space!
                	PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(worldserver, blockpos);
                	if(wrapper != null) {
                		//Fix for Chisels and Bits
                    	RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform, playerHitVec);
                	}
                }

            	this.player.interactionManager.processRightClickBlock(this.player, worldserver, itemstack, enumhand, blockpos, enumfacing, (float) playerHitVec.X, (float) playerHitVec.Y, (float) playerHitVec.Z);
            }
        } else {
            TextComponentTranslation textcomponenttranslation = new TextComponentTranslation("build.tooHigh", new Object[]{Integer.valueOf(thisClassAsAHandler.serverController.getBuildLimit())});
            textcomponenttranslation.getStyle().setColor(TextFormatting.RED);
            this.player.connection.sendPacket(new SPacketChat(textcomponenttranslation, (byte) 2));
        }

        this.player.connection.sendPacket(new SPacketBlockChange(worldserver, blockpos));
        this.player.connection.sendPacket(new SPacketBlockChange(worldserver, blockpos.offset(enumfacing)));
    }

    public void processPlayerDiggingOriginal(CPacketPlayerDigging packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, thisClassAsAHandler, thisClassAsAHandler.player.getServerWorld());
        WorldServer worldserver = thisClassAsAHandler.serverController.worldServerForDimension(this.player.dimension);
        BlockPos blockpos = packetIn.getPosition();
        this.player.markPlayerActive();

        switch (packetIn.getAction()) {
            case SWAP_HELD_ITEMS:

                if (!this.player.isSpectator()) {
                    ItemStack itemstack = this.player.getHeldItem(EnumHand.OFF_HAND);
                    this.player.setHeldItem(EnumHand.OFF_HAND, this.player.getHeldItem(EnumHand.MAIN_HAND));
                    this.player.setHeldItem(EnumHand.MAIN_HAND, itemstack);
                }

                return;
            case DROP_ITEM:

                if (!this.player.isSpectator()) {
                    this.player.dropItem(false);
                }

                return;
            case DROP_ALL_ITEMS:

                if (!this.player.isSpectator()) {
                    this.player.dropItem(true);
                }

                return;
            case RELEASE_USE_ITEM:
                this.player.stopActiveHand();
                return;
            case START_DESTROY_BLOCK:
            case ABORT_DESTROY_BLOCK:
            case STOP_DESTROY_BLOCK:
                double d0 = this.player.posX - ((double) blockpos.getX() + 0.5D);
                double d1 = this.player.posY - ((double) blockpos.getY() + 0.5D) + 1.5D;
                double d2 = this.player.posZ - ((double) blockpos.getZ() + 0.5D);
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                double dist = player.interactionManager.getBlockReachDistance() + 1;
                dist *= dist;

                if (d3 > dist) {
                    return;
                } else if (blockpos.getY() >= thisClassAsAHandler.serverController.getBuildLimit()) {
                    return;
                } else {
                    if (packetIn.getAction() == CPacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                        if (!thisClassAsAHandler.serverController.isBlockProtected(worldserver, blockpos, this.player) && worldserver.getWorldBorder().contains(blockpos)) {
                            this.player.interactionManager.onBlockClicked(blockpos, packetIn.getFacing());
                        } else {
                            this.player.connection.sendPacket(new SPacketBlockChange(worldserver, blockpos));
                        }
                    } else {
                        if (packetIn.getAction() == CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK) {
                            this.player.interactionManager.blockRemoving(blockpos);
                        } else if (packetIn.getAction() == CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK) {
                            this.player.interactionManager.cancelDestroyingBlock();
                        }

                        if (worldserver.getBlockState(blockpos).getMaterial() != Material.AIR) {
                            this.player.connection.sendPacket(new SPacketBlockChange(worldserver, blockpos));
                        }
                    }

                    return;
                }

            default:
                throw new IllegalArgumentException("Invalid player action");
        }
    }

    public void processUpdateSignOriginal(CPacketUpdateSign packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, thisClassAsAHandler, thisClassAsAHandler.player.getServerWorld());
        this.player.markPlayerActive();
        WorldServer worldserver = thisClassAsAHandler.serverController.worldServerForDimension(this.player.dimension);
        BlockPos blockpos = packetIn.getPosition();

        if (worldserver.isBlockLoaded(blockpos)) {
            IBlockState iblockstate = worldserver.getBlockState(blockpos);
            TileEntity tileentity = worldserver.getTileEntity(blockpos);

            if (!(tileentity instanceof TileEntitySign)) {
                return;
            }

            TileEntitySign tileentitysign = (TileEntitySign) tileentity;

            if (!tileentitysign.getIsEditable() || tileentitysign.getPlayer() != this.player) {
                thisClassAsAHandler.serverController.logWarning("Player " + this.player.getName() + " just tried to change non-editable sign");
                return;
            }

            String[] astring = packetIn.getLines();

            for (int i = 0; i < astring.length; ++i) {
                tileentitysign.signText[i] = new TextComponentString(TextFormatting.getTextWithoutFormattingCodes(astring[i]));
            }

            tileentitysign.markDirty();
            worldserver.notifyBlockUpdate(blockpos, iblockstate, iblockstate, 3);
        }
    }

}
