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
import valkyrienwarfare.addon.control.nodenetwork.VWNode_TileEntity;
import valkyrienwarfare.addon.control.piloting.ControllerInputType;
import valkyrienwarfare.addon.control.piloting.PilotControlsMessage;

public class TileEntityLiftControl extends ImplTileEntityPilotable {

	private int liftPercentage;
	private int nextLiftPercentage;
	
	// Between 0 and 1, where .5 is the middle.
	private float leverOffset;
	private float nextLeverOffset;
	private float prevLeverOffset;
	
	public TileEntityLiftControl() {
		super();
		this.liftPercentage = 0;
		this.nextLiftPercentage = 0;
		this.leverOffset = .5f;
		this.nextLeverOffset = .5f;
		this.prevLeverOffset = .5f;
	}
	
	@Override
	ControllerInputType getControlInputType() {
		return ControllerInputType.LiftControl;
	}

	@Override
	public void update() {
		if (this.getWorld().isRemote) {
			this.liftPercentage = nextLiftPercentage;
			this.prevLeverOffset = this.leverOffset;
			this.leverOffset = (float) (((nextLeverOffset - leverOffset) * .7) + leverOffset);
		} else {
			sendUpdatePacketToAllNearby();
			VWNode_TileEntity thisNode = this.getNode();
			for (GraphObject object : thisNode.getGraph().getObjects()) {
				VWNode_TileEntity otherNode = (VWNode_TileEntity) object;
				TileEntity tile = otherNode.getParentTile();
				if (tile instanceof TileEntityEtherCompressorPanel) {
					BlockPos masterPos = ((TileEntityEtherCompressorPanel) tile).getMasterPos();
					TileEntityEtherCompressorPanel masterTile = (TileEntityEtherCompressorPanel) tile.getWorld().getTileEntity(masterPos);
					// This is a transient problem that only occurs during world loading.
					if (masterTile != null) {
						masterTile.setThrustMultiplierGoal(((double) liftPercentage) / 100D);
						masterTile.updateTicksSinceLastRecievedSignal();
					}
				}
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Override
    public void renderPilotText(FontRenderer renderer, ScaledResolution gameResolution) {
		// White text.
		int color = 0xFFFFFF;
		// Extra spaces so the that the text is closer to the middle when rendered.
		String message = "Power:    ";
		int i = gameResolution.getScaledWidth();
        int height = gameResolution.getScaledHeight() - 35;
		float middle = (float)(i / 2 - renderer.getStringWidth(message) / 2);
		message = "Power: " + liftPercentage + "%";
		renderer.drawStringWithShadow(message , middle, height, color);
    }
	
	@Override
	void processControlMessage(PilotControlsMessage message, EntityPlayerMP sender) {
		final float controlRate = .075f;
		
		if (message.airshipForward_KeyDown) {
			liftPercentage++;
			leverOffset += controlRate;
			// System.out.println("Lift Up");
		}
		if (message.airshipBackward_KeyDown) {
			liftPercentage--;
			leverOffset -= controlRate;
			// System.out.println("Lift Down");
		}
		
		leverOffset = Math.max(0, Math.min(1, leverOffset));
		liftPercentage = Math.min(100, Math.max(0, liftPercentage));
		
		if (!message.airshipForward_KeyDown && !message.airshipBackward_KeyDown) {
			if (leverOffset > .5 + controlRate) {
				leverOffset -= controlRate;
			} else if (leverOffset < .5 - controlRate) {
				leverOffset += controlRate;
			} else {
				leverOffset = .5f;
			}
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound toReturn = super.writeToNBT(compound);
		compound.setInteger("liftPercentage", liftPercentage);
		compound.setFloat("leverOffset", leverOffset);
		return toReturn;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		liftPercentage = compound.getInteger("liftPercentage");
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		nextLiftPercentage = pkt.getNbtCompound().getInteger("liftPercentage");
		nextLeverOffset = pkt.getNbtCompound().getFloat("leverOffset");
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound toReturn = super.getUpdateTag();
		toReturn.setInteger("liftPercentage", liftPercentage);
		toReturn.setFloat("leverOffset", leverOffset);
		return toReturn;
	}
	
	public int getLiftPercentage() {
		return liftPercentage;
	}
	
	public int getNextLiftPercentage() {
		return nextLiftPercentage;
	}
	
	public float getLeverOffset() {
		return leverOffset;
	}
	
	public float getNextLeverOffet() {
		return nextLeverOffset;
	}
	
	public float getPrevLeverOffset() {
		return prevLeverOffset;
	}

}
