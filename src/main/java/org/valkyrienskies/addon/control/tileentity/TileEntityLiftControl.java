package org.valkyrienskies.addon.control.tileentity;

import gigaherz.graph.api.GraphObject;
import java.util.Optional;
import net.minecraft.block.state.IBlockState;
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
import org.valkyrienskies.addon.control.block.multiblocks.TileEntityEthereumCompressorPart;
import org.valkyrienskies.addon.control.nodenetwork.VWNode_TileEntity;
import org.valkyrienskies.addon.control.piloting.ControllerInputType;
import org.valkyrienskies.addon.control.piloting.PilotControlsMessage;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

public class TileEntityLiftControl extends TileEntityPilotableImpl {

    private static final double LEVER_PULL_RATE = .075D;
    // Between 0 and 1, where .5 is the middle.
    private float leverOffset;
    // Used by the client to smoothly render the lever animation
    private float nextLeverOffset;
    private float prevLeverOffset;
    // The height this lever wants to be at
    private double targetYPosition;
    // Assigned by onPilotsMessage(), when true the lever changes the reference height 5x quicker.
    private boolean isPilotSprinting;
    // The number of consecutive ticks the pilot has been sprinting
    private int pilotSprintTicks;
    // Used to tell the lift control when to set its target height to be the current height of the ship.
    private boolean hasHeightBeenSet;

    public TileEntityLiftControl() {
        super();
        this.leverOffset = .5f;
        this.nextLeverOffset = .5f;
        this.prevLeverOffset = .5f;
        this.targetYPosition = 0;
        this.isPilotSprinting = false;
        this.pilotSprintTicks = 0;
        this.hasHeightBeenSet = false;
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
            if (!hasHeightBeenSet) {
                Optional<PhysicsObject> physicsObject = ValkyrienUtils
                    .getPhysicsObject(getWorld(), getPos());
                if (physicsObject.isPresent()) {
                    Vector currentPos = new Vector(getPos().getX() + .5, getPos().getY() + .5,
                        getPos().getZ() + .5);
                    physicsObject.get()
                        .getShipTransformationManager()
                        .getCurrentTickTransform()
                        .transform(currentPos, TransformType.SUBSPACE_TO_GLOBAL);
                    targetYPosition = currentPos.Y;
                } else {
                    targetYPosition = getPos().getY() + .5;
                }
                hasHeightBeenSet = true;
            }
            if (this.getPilotEntity() == null) {
                leverOffset += .5 * (.5 - leverOffset);
            } else {
                this.markDirty();
            }
            if (!isPilotSprinting) {
                targetYPosition += (leverOffset - .5) / 2D;
            } else {
                targetYPosition += (leverOffset - .5) * 1.25D;
            }

            VWNode_TileEntity thisNode = this.getNode();
            Optional<PhysicsObject> physicsObject = ValkyrienUtils
                .getPhysicsObject(getWorld(), getPos());

            if (physicsObject.isPresent()) {
                // The linear velocity of the ship
                Vector linearVel = physicsObject.get()
                    .getPhysicsProcessor()
                    .getVelocityAtPoint(new Vector());
                // The global coordinates of this tile entity
                Vector tilePos = new Vector(getPos().getX() + .5, getPos().getY() + .5,
                    getPos().getZ() + .5);
                physicsObject.get()
                    .getShipTransformationManager()
                    .getCurrentPhysicsTransform()
                    .transform(tilePos, TransformType.SUBSPACE_TO_GLOBAL);

                // Utilizing a proper PI controller for very smooth control.
                double heightWithIntegral = tilePos.Y + linearVel.Y * .3D;
                double heightDelta = targetYPosition - heightWithIntegral;
                double multiplier = heightDelta / 2D;
                multiplier = Math.max(0, Math.min(1, multiplier));

                for (GraphObject object : thisNode.getGraph().getObjects()) {
                    VWNode_TileEntity otherNode = (VWNode_TileEntity) object;
                    TileEntity tile = otherNode.getParentTile();
                    if (tile instanceof TileEntityEthereumCompressorPart) {
                        BlockPos masterPos = ((TileEntityEthereumCompressorPart) tile)
                            .getMultiblockOrigin();
                        TileEntityEthereumCompressorPart masterTile = (TileEntityEthereumCompressorPart) tile
                            .getWorld().getTileEntity(masterPos);
                        // This is a transient problem that only occurs during world loading.
                        if (masterTile != null) {
                            masterTile.setThrustMultiplierGoal(multiplier);
                        }
                    }
                }
            }

            IBlockState blockState = getWorld().getBlockState(getPos());
            getWorld().notifyBlockUpdate(getPos(), blockState, blockState, 0);
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
        message = "Target Altitude: " + Math.round(targetYPosition);
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
            leverOffset += LEVER_PULL_RATE;
            if (pilotSprintTicks > 0 && pilotSprintTicks < 5) {
                leverOffset += 20 * LEVER_PULL_RATE;
            }
        }
        if (message.airshipBackward_KeyDown) {
            // liftPercentage--;
            leverOffset -= LEVER_PULL_RATE;
            if (pilotSprintTicks > 0 && pilotSprintTicks < 5) {
                leverOffset -= 20 * LEVER_PULL_RATE;
            }
        }

        if (!message.airshipForward_KeyDown && !message.airshipBackward_KeyDown) {
            if (leverOffset > .5 + LEVER_PULL_RATE) {
                leverOffset -= LEVER_PULL_RATE / 2;
            } else if (leverOffset < .5 - LEVER_PULL_RATE) {
                leverOffset += LEVER_PULL_RATE / 2;
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
        compound.setDouble("targetYPosition", targetYPosition);
        compound.setBoolean("hasHeightBeenSet", hasHeightBeenSet);
        return toReturn;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        leverOffset = compound.getFloat("leverOffset");
        targetYPosition = compound.getDouble("targetYPosition");
        hasHeightBeenSet = compound.getBoolean("hasHeightBeenSet");
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        nextLeverOffset = pkt.getNbtCompound().getFloat("leverOffset");
        targetYPosition = pkt.getNbtCompound()
            .getDouble("targetYPosition");
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound toReturn = super.getUpdateTag();
        toReturn.setFloat("leverOffset", leverOffset);
        toReturn.setDouble("targetYPosition", targetYPosition);
        return toReturn;
    }

    public float getLeverOffset() {
        return leverOffset;
    }

    public float getPrevLeverOffset() {
        return prevLeverOffset;
    }

}
