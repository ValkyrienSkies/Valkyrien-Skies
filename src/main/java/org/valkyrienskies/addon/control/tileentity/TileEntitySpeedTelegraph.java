package org.valkyrienskies.addon.control.tileentity;

import gigaherz.graph.api.GraphObject;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.valkyrienskies.addon.control.block.BlockSpeedTelegraph;
import org.valkyrienskies.addon.control.nodenetwork.VSNode_TileEntity;
import org.valkyrienskies.addon.control.piloting.ControllerInputType;
import org.valkyrienskies.addon.control.piloting.PilotControlsMessage;
import org.valkyrienskies.fixes.VSNetwork;

public class TileEntitySpeedTelegraph extends TileEntityPilotableImpl implements ITickable {

    private ShipChadburnState telegraphState;
    // The following fields are only used by the client for smooth interpolation
    // rendering between state enums.
    private ShipChadburnState nextTelegraphState;
    private double handleRotation;
    private double prevHandleRotation;

    public TileEntitySpeedTelegraph() {
        this.telegraphState = ShipChadburnState.STOP;
        this.nextTelegraphState = ShipChadburnState.STOP;
        this.handleRotation = 0;
        this.prevHandleRotation = 0;
    }

    @Override
    void processControlMessage(PilotControlsMessage message, EntityPlayerMP sender) {
        int deltaOrdinal = 0;
        if (message.airshipLeft_KeyPressed) {
            deltaOrdinal -= 1;
        }
        if (message.airshipRight_KeyPressed) {
            deltaOrdinal += 1;
        }
        IBlockState blockState = this.getWorld().getBlockState(getPos());
        if (blockState.getBlock() instanceof BlockSpeedTelegraph) {
            EnumFacing facing = blockState.getValue(BlockSpeedTelegraph.FACING);
            if (this.isPlayerInFront(sender, facing)) {
                deltaOrdinal *= -1;
            }
        }
        int newTelegraphOrdinal = telegraphState.ordinal();
        newTelegraphOrdinal += deltaOrdinal;
        newTelegraphOrdinal = Math
            .max(0, Math.min(ShipChadburnState.values().length - 1, newTelegraphOrdinal));
        telegraphState = ShipChadburnState.values()[newTelegraphOrdinal];
        this.markDirty();
    }


    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net,
        net.minecraft.network.play.server.SPacketUpdateTileEntity pkt) {
        nextTelegraphState = ShipChadburnState
            .valueOf(pkt.getNbtCompound().getString("TelegraphState"));
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tagToSend = new NBTTagCompound();
        tagToSend.setString("TelegraphState", telegraphState.name());
        return new SPacketUpdateTileEntity(this.getPos(), 0, tagToSend);
    }

    public double getHandleRenderRotation(float partialTicks) {
        double interpolatedHandle =
            prevHandleRotation + (handleRotation - prevHandleRotation) * partialTicks;
        return -interpolatedHandle + 112.5;
    }

    @Override
    public void update() {
        if (this.getNode() == null || this.getNode()
            .getGraph() == null) {
            this.tileEntityInvalid = true;
        }
        if (getWorld().isRemote) {
            this.prevHandleRotation = this.handleRotation;
            this.handleRotation = this.handleRotation
                + (this.nextTelegraphState.renderRotation - this.handleRotation) * .5;
            this.telegraphState = nextTelegraphState;
        } else {
            Collection<GraphObject> connectedGraphObjects = getNode().getGraph()
                .getObjects();
            if (connectedGraphObjects == null) {
                new IllegalStateException(
                    "Graph object neighbors are null! Skipping ship telegraph update.")
                    .printStackTrace();
                return;
            }
            for (GraphObject object : connectedGraphObjects) {
                VSNode_TileEntity otherNode = (VSNode_TileEntity) object;
                TileEntity tile = otherNode.getParentTile();
                if (tile instanceof TileEntityGearbox) {
                    TileEntityGearbox masterTile = (TileEntityGearbox) tile;
                    // This is a transient problem that only occurs during world loading.
                    if (telegraphState == ShipChadburnState.STOP) {
                        masterTile.setOutputRatio(Optional.empty());
                    } else {
                        masterTile.setOutputRatio(Optional.of(telegraphState.gearboxOutputRatio));
                    }
                }
            }
            VSNetwork.sendTileToAllNearby(this);
        }
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound toReturn = super.getUpdateTag();
        return toReturn;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        telegraphState = ShipChadburnState.valueOf(compound.getString("TelegraphState"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound toReturn = super.writeToNBT(compound);
        toReturn.setString("TelegraphState", telegraphState.name());
        return toReturn;
    }

    @Override
    ControllerInputType getControlInputType() {
        return ControllerInputType.Telegraph;
    }

    private enum ShipChadburnState {

        FULL_AHEAD(-120, 4), HALF_AHEAD(-80, 2), SLOW_AHEAD(-40, 1), STOP(0, 0), SLOW_ASTERN(40,
            -1), HALF_ASTERN(80, -2), FULL_ASTERN(120, -4);

        // The rotation in degrees in the clockwise direction relative to midnight.
        public final double renderRotation;
        public final double gearboxOutputRatio;

        ShipChadburnState(double renderRotation, double gearboxOutputRatio) {
            this.renderRotation = renderRotation;
            this.gearboxOutputRatio = gearboxOutputRatio;
        }
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos()).expand(0, 1, 0);
    }
}
