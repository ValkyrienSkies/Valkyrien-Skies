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
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.control.block.multiblocks.TileEntityEthereumCompressorPart;
import valkyrienwarfare.addon.control.nodenetwork.VWNode_TileEntity;
import valkyrienwarfare.addon.control.piloting.ControllerInputType;
import valkyrienwarfare.addon.control.piloting.PilotControlsMessage;
import valkyrienwarfare.fixes.VWNetwork;
import valkyrienwarfare.math.Vector;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

public class TileEntityLiftControl extends ImplTileEntityPilotable {

    public static final double leverPullRate = .075D;
    // Between 0 and 1, where .5 is the middle.
    private float leverOffset;
    private float nextLeverOffset;
    private float prevLeverOffset;

    private double heightReference;
    // Assigned by onPilotsMessage(), when true the lever changes the reference height 5x quicker.
    private boolean isPilotSprinting;
    private int pilotSprintTicks;

    public TileEntityLiftControl() {
        super();
        this.leverOffset = .5f;
        this.nextLeverOffset = .5f;
        this.prevLeverOffset = .5f;
        this.heightReference = 0;
        this.isPilotSprinting = false;
        this.pilotSprintTicks = 0;
    }

    public TileEntityLiftControl(World world) {
        this();
        // When placed in the world set target height to be the sea level + 10.
        this.heightReference = world.getSeaLevel() + 10;
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
            if (this.getPilotEntity() == null) {
                leverOffset += .5 * (.5 - leverOffset);
            } else {
                this.markDirty();
            }
            if (!isPilotSprinting) {
                heightReference += (leverOffset - .5) / 2D;
            } else {
                heightReference += (leverOffset - .5) * 1.25D;
            }

            VWNode_TileEntity thisNode = this.getNode();
            PhysicsWrapperEntity parentEntity = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getObjectManagingPos(this.getWorld(), this.getPos());

            if (parentEntity != null) {
                Vector linearVel = parentEntity.getPhysicsObject().getPhysicsProcessor().getVelocityAtPoint(new Vector());
                Vector physPos = parentEntity.getPhysicsObject().getPhysicsProcessor().getCopyOfPhysCoordinates();

                double totalMaxUpwardThrust = 0;
                for (GraphObject object : thisNode.getGraph().getObjects()) {
                    VWNode_TileEntity otherNode = (VWNode_TileEntity) object;
                    TileEntity tile = otherNode.getParentTile();
                    if (tile instanceof TileEntityEthereumCompressorPart) {
                        BlockPos masterPos = ((TileEntityEthereumCompressorPart) tile).getMultiblockOrigin();
                        TileEntityEthereumCompressorPart masterTile = (TileEntityEthereumCompressorPart) tile.getWorld().getTileEntity(masterPos);
                        // This is a transient problem that only occurs during world loading.
                        if (masterTile != null && parentEntity != null) {
                            totalMaxUpwardThrust += masterTile.getMaxThrust();
                        }
                        // masterTile.updateTicksSinceLastRecievedSignal();
                    }
                }

                // Utilizing a proper PI controller for very smooth control.
                double heightWithIntegral = physPos.Y + linearVel.Y * .3D;
                double heightDelta = heightReference - heightWithIntegral;
                double multiplier = heightDelta / 2D;
                multiplier = Math.max(0, Math.min(1, multiplier));

                for (GraphObject object : thisNode.getGraph().getObjects()) {
                    VWNode_TileEntity otherNode = (VWNode_TileEntity) object;
                    TileEntity tile = otherNode.getParentTile();
                    if (tile instanceof TileEntityEthereumCompressorPart) {
                        BlockPos masterPos = ((TileEntityEthereumCompressorPart) tile).getMultiblockOrigin();
                        TileEntityEthereumCompressorPart masterTile = (TileEntityEthereumCompressorPart) tile.getWorld().getTileEntity(masterPos);
                        // This is a transient problem that only occurs during world loading.
                        if (masterTile != null && parentEntity != null) {
                            masterTile.setThrustMultiplierGoal(multiplier);
                        }
                        // masterTile.updateTicksSinceLastRecievedSignal();
                    }
                }
            }

            VWNetwork.sendTileToAllNearby(this);
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
        float middle = (float) (i / 2 - renderer.getStringWidth(message) / 2);
        message = "Target Altitude: " + Math.round(heightReference);
        renderer.drawStringWithShadow(message, middle, height, color);
    }

    @Override
    void processControlMessage(PilotControlsMessage message, EntityPlayerMP sender) {
        isPilotSprinting = message.airshipSprinting;
        if (isPilotSprinting) {
            pilotSprintTicks++;
        } else {
            pilotSprintTicks = 0;
        }

        if (message.airshipForward_KeyDown) {
            // liftPercentage++;
            leverOffset += leverPullRate;
            if (pilotSprintTicks > 0 && pilotSprintTicks < 5) {
                leverOffset += 20 * leverPullRate;
            }
        }
        if (message.airshipBackward_KeyDown) {
            // liftPercentage--;
            leverOffset -= leverPullRate;
            if (pilotSprintTicks > 0 && pilotSprintTicks < 5) {
                leverOffset -= 20 * leverPullRate;
            }
        }

        if (!message.airshipForward_KeyDown && !message.airshipBackward_KeyDown) {
            if (leverOffset > .5 + leverPullRate) {
                leverOffset -= leverPullRate / 2;
            } else if (leverOffset < .5 - leverPullRate) {
                leverOffset += leverPullRate / 2;
            } else {
                leverOffset = .5f;
            }
        }

        if (message.airshipSprinting) {
            if (pilotSprintTicks > 0 && pilotSprintTicks < 5) {
                leverOffset = Math.max(0f, Math.min(1f, leverOffset));
            } else {
                leverOffset = Math.max(.1f, Math.min(.9f, leverOffset));
            }
        } else {
            leverOffset = Math.max(.25f, Math.min(.75f, leverOffset));
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
