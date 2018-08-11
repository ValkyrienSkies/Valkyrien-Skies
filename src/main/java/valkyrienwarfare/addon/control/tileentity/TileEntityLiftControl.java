package valkyrienwarfare.addon.control.tileentity;

import gigaherz.graph.api.GraphObject;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.control.block.multiblocks.TileEntityEthereumCompressorPart;
import valkyrienwarfare.addon.control.nodenetwork.VWNode_TileEntity;
import valkyrienwarfare.addon.control.piloting.ControllerInputType;
import valkyrienwarfare.addon.control.piloting.PilotControlsMessage;
import valkyrienwarfare.math.Vector;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

public class TileEntityLiftControl extends ImplTileEntityPilotable {
	
	// Between 0 and 1, where .5 is the middle.
	private float leverOffset;
	private float nextLeverOffset;
	private float prevLeverOffset;
	
	private double heightReference;
	
	public TileEntityLiftControl() {
		super();
		this.leverOffset = .5f;
		this.nextLeverOffset = .5f;
		this.prevLeverOffset = .5f;
		this.heightReference = 0;
	}
	
	@Override
	ControllerInputType getControlInputType() {
		return ControllerInputType.LiftControl;
	}

	@Override
	public void update() {
		if (this.getWorld().isRemote) {
			this.prevLeverOffset = this.leverOffset;
			this.leverOffset = (float) (((nextLeverOffset - leverOffset) * .7) + leverOffset);
		} else {
			sendUpdatePacketToAllNearby();
			VWNode_TileEntity thisNode = this.getNode();
			PhysicsWrapperEntity parentEntity = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getObjectManagingPos(this.getWorld(), this.getPos());
			for (GraphObject object : thisNode.getGraph().getObjects()) {
				VWNode_TileEntity otherNode = (VWNode_TileEntity) object;
				TileEntity tile = otherNode.getParentTile();
				if (tile instanceof TileEntityEthereumCompressorPart) {
					BlockPos masterPos = ((TileEntityEthereumCompressorPart) tile).getMultiblockOrigin();
					TileEntityEthereumCompressorPart masterTile = (TileEntityEthereumCompressorPart) tile.getWorld().getTileEntity(masterPos);
					// This is a transient problem that only occurs during world loading.
					if (masterTile != null && parentEntity != null) {
						double shipYHeight = parentEntity.getPhysicsObject().getWrapperEntity().posY;
						double shipYVelocity = parentEntity.getPhysicsObject().getPhysicsProcessor().getVelocityAtPoint(new Vector()).Y;
						
						double effectiveHeight = shipYHeight + shipYVelocity;
						
						double controlOffset = this.heightReference - effectiveHeight;

						// Simple impulse control scheme.
						if (controlOffset > 0) {
							masterTile.setThrustMultiplierGoal(1);
						} else {
							masterTile.setThrustMultiplierGoal(0);
						}
					}
					// masterTile.updateTicksSinceLastRecievedSignal();
				}
			}
			
			if (this.getPilotEntity() == null) {
				this.leverOffset = .5f;
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Override
    public void renderPilotText(FontRenderer renderer, ScaledResolution gameResolution) {
		// White text.
		int color = 0xFFFFFF;
		// Extra spaces so the that the text is closer to the middle when rendered.
		String message = "Target Altitude:    ";
		int i = gameResolution.getScaledWidth();
        int height = gameResolution.getScaledHeight() - 35;
		float middle = (float)(i / 2 - renderer.getStringWidth(message) / 2);
		message = "Target Altitude: " + heightReference;
		renderer.drawStringWithShadow(message , middle, height, color);
    }
	
	@Override
	void processControlMessage(PilotControlsMessage message, EntityPlayerMP sender) {
		final float leverPullRate = .075f;
		
		if (message.airshipForward_KeyDown) {
			// liftPercentage++;
			leverOffset += leverPullRate;
			heightReference += .5;
		}
		if (message.airshipBackward_KeyDown) {
			// liftPercentage--;
			leverOffset -= leverPullRate;
			heightReference -= .5;
		}
		
		leverOffset = Math.max(0, Math.min(1, leverOffset));
		
		if (!message.airshipForward_KeyDown && !message.airshipBackward_KeyDown) {
			if (leverOffset > .5 + leverPullRate) {
				leverOffset -= leverPullRate;
			} else if (leverOffset < .5 - leverPullRate) {
				leverOffset += leverPullRate;
			} else {
				leverOffset = .5f;
			}
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound toReturn = super.writeToNBT(compound);
		compound.setFloat("leverOffset", leverOffset);
		compound.setDouble("heightReference", heightReference);
		return toReturn;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		leverOffset = compound.getFloat("leverOffset");
		heightReference = compound.getDouble("heightReference");
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		nextLeverOffset = pkt.getNbtCompound().getFloat("leverOffset");
		heightReference = pkt.getNbtCompound().getDouble("heightReference");
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound toReturn = super.getUpdateTag();
		toReturn.setFloat("leverOffset", leverOffset);
		toReturn.setDouble("heightReference", heightReference);
		return toReturn;
	}
	
	public float getLeverOffset() {
		return leverOffset;
	}
	
	public float getPrevLeverOffset() {
		return prevLeverOffset;
	}

}
