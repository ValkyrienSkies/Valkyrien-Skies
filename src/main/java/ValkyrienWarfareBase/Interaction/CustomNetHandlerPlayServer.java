package ValkyrienWarfareBase.Interaction;

import java.lang.reflect.Field;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.Math.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.client.CPacketPlayerBlockPlacement;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;

public class CustomNetHandlerPlayServer extends NetHandlerPlayServer{

	public CustomNetHandlerPlayServer(MinecraftServer server, NetworkManager networkManagerIn, EntityPlayerMP playerIn) {
		super(server, networkManagerIn, playerIn);
	}
	
	public CustomNetHandlerPlayServer(NetHandlerPlayServer toReplace) {
		super(toReplace.serverController, toReplace.netManager, toReplace.playerEntity);
		networkTickCount = toReplace.networkTickCount;
	    field_147378_h = toReplace.field_147378_h;
	    lastPingTime = toReplace.lastPingTime;
	    lastSentPingPacket = toReplace.lastSentPingPacket;
	    chatSpamThresholdCount = toReplace.chatSpamThresholdCount;
	    itemDropThreshold = toReplace.itemDropThreshold;
	    
	    try{	    	
	    	Field intHashMapField = this.getClass().getDeclaredField("field_147372_n");
	    	intHashMapField.setAccessible(true);
	    	intHashMapField.set(this, toReplace.field_147372_n);
	    }catch(Exception e){}
	    
	    field_184349_l = toReplace.field_184349_l;
	    field_184350_m = toReplace.field_184350_m;
	    field_184351_n = toReplace.field_184351_n;
	    field_184352_o = toReplace.field_184352_o;
	    field_184353_p = toReplace.field_184353_p;
	    field_184354_q = toReplace.field_184354_q;
	    lowestRiddenEnt = toReplace.lowestRiddenEnt;
	    lowestRiddenX = toReplace.lowestRiddenX;
	    lowestRiddenY = toReplace.lowestRiddenY;
	    lowestRiddenZ = toReplace.lowestRiddenZ;
	    lowestRiddenX1 = toReplace.lowestRiddenX1;
	    lowestRiddenY1 = toReplace.lowestRiddenY1;
	    lowestRiddenZ1 = toReplace.lowestRiddenZ1;
	    field_184362_y = toReplace.field_184362_y;
	    teleportId = toReplace.teleportId;
	    field_184343_A = toReplace.field_184343_A;
	    floating = toReplace.floating;
	    floatingTickCount = toReplace.floatingTickCount;
	    vehicleFloating = toReplace.vehicleFloating;
	    field_184346_E = toReplace.field_184346_E;
	    field_184347_F = toReplace.field_184347_F;
	    field_184348_G = toReplace.field_184348_G;
	}
	
	@Override
    public void processPlayerDigging(CPacketPlayerDigging packetIn){
		super.processPlayerDigging(packetIn);
	}
	
	@Override
	public void processRightClickBlock(CPacketPlayerTryUseItem packetIn)
    {
		PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        WorldServer worldserver = this.serverController.worldServerForDimension(this.playerEntity.dimension);
        EnumHand enumhand = packetIn.getHand();
        ItemStack itemstack = this.playerEntity.getHeldItem(enumhand);
        BlockPos blockpos = packetIn.getPos();
        EnumFacing enumfacing = packetIn.func_187024_b();
        this.playerEntity.markPlayerActive();

        if (blockpos.getY() < this.serverController.getBuildLimit() - 1 || enumfacing != EnumFacing.UP && blockpos.getY() < this.serverController.getBuildLimit())
        {
            double dist = playerEntity.interactionManager.getBlockReachDistance() + 3;
            dist *= dist;
            
            Vector blockPosToPlayer = new Vector((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D);
            
            PhysicsWrapperEntity owner = ValkyrienWarfareMod.physicsManager.getObjectManagingChunk(playerEntity.worldObj.getChunkFromBlockCoords(blockpos));
            
            if(owner!=null){
            	owner.wrapping.coordTransform.fromLocalToGlobal(blockPosToPlayer);
            }
            
            if (this.field_184362_y == null && this.playerEntity.getDistanceSq(blockPosToPlayer.X,blockPosToPlayer.Y,blockPosToPlayer.Z) < dist && !this.serverController.isBlockProtected(worldserver, blockpos, this.playerEntity) && worldserver.getWorldBorder().contains(blockpos))
            {
                this.playerEntity.interactionManager.processRightClickBlock(this.playerEntity, worldserver, itemstack, enumhand, blockpos, enumfacing, packetIn.func_187026_d(), packetIn.func_187025_e(), packetIn.func_187020_f());
            }
        }
        else
        {
            TextComponentTranslation textcomponenttranslation = new TextComponentTranslation("build.tooHigh", new Object[] {Integer.valueOf(this.serverController.getBuildLimit())});
            textcomponenttranslation.getChatStyle().setColor(TextFormatting.RED);
            this.playerEntity.playerNetServerHandler.sendPacket(new SPacketChat(textcomponenttranslation));
        }

        this.playerEntity.playerNetServerHandler.sendPacket(new SPacketBlockChange(worldserver, blockpos));
        this.playerEntity.playerNetServerHandler.sendPacket(new SPacketBlockChange(worldserver, blockpos.offset(enumfacing)));
        itemstack = this.playerEntity.getHeldItem(enumhand);

        if (itemstack != null && itemstack.stackSize == 0)
        {
            this.playerEntity.setHeldItem(enumhand, (ItemStack)null);
            net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(this.playerEntity, itemstack, enumhand);
            itemstack = null;
        }
    }
	
	@Override
	public void processPlayerBlockPlacement(CPacketPlayerBlockPlacement packetIn)
    {
		super.processPlayerBlockPlacement(packetIn);
    }
	
	@Override
	public void processUseEntity(CPacketUseEntity packetIn){
		super.processUseEntity(packetIn);
	}
}
