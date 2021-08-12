package org.valkyrienskies.mod.common.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.valkyrienskies.mod.common.piloting.PilotControlsMessage;

public class TileEntitySmallShipSail extends TileEntityBoatChair implements ITickable {

    private static final int ROTATION_PACKET_INTERVAL = 5;

    private double currentRotation = 0.0;
    // Used by client code to interpolate
    private double prevTickRotation = 0.0;
    private int ticksToSmoothlyInterpolate = 0;
    private double angleToInterpolatePerTick = 0.0;
    // Used by server to send updates
    private int ticksSinceLastUpdate = 0;
    private boolean changedSinceLastUpdate = false;

    @Override
    public void update() {
        if (world.isRemote) {
            // Client code
            prevTickRotation = currentRotation;
            if (ticksToSmoothlyInterpolate > 0) {
                currentRotation += angleToInterpolatePerTick;
                ticksToSmoothlyInterpolate--;
            }
        } else {
            // Server code
            ticksSinceLastUpdate++;
            if (ticksSinceLastUpdate >= ROTATION_PACKET_INTERVAL && changedSinceLastUpdate) {
                ticksSinceLastUpdate = 0;
                changedSinceLastUpdate = false;
                // Sends clients an update packet
                final IBlockState blockState = world.getBlockState(pos);
                world.notifyBlockUpdate(pos, blockState, blockState, 0);
            }
        }
    }

    private void setRotationAngle(double rotationAngle) {
        currentRotation = rotationAngle;
        changedSinceLastUpdate = true;
    }

    public double getRotationForRendering(float partialTicks) {
        return prevTickRotation + (currentRotation - prevTickRotation) * partialTicks + 180;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setDouble("current_rotation", currentRotation);
        return super.writeToNBT(compound);
    }

    public void readFromNBT(NBTTagCompound compound) {
        currentRotation = compound.getDouble("current_rotation");
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        final double nextRotation = pkt.getNbtCompound().getDouble("current_rotation");
        ticksToSmoothlyInterpolate = 10;
        angleToInterpolatePerTick = (nextRotation - currentRotation) / ticksToSmoothlyInterpolate;
    }

    @Override
    public void processControlMessage(PilotControlsMessage message, EntityPlayerMP sender) {
        super.processControlMessage(message, sender);

        if (message.airshipLeft_KeyDown) {
            setRotationAngle(Math.min(currentRotation + 1, 90));
        }
        if (message.airshipRight_KeyDown) {
            setRotationAngle(Math.max(currentRotation - 1, -90));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public net.minecraft.util.math.AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos.add(-3, -3, -3), pos.add(3, 10, 3));
    }
}
