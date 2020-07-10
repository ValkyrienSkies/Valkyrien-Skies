package org.valkyrienskies.addon.control.tileentity;

import gigaherz.graph.api.GraphObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.joml.Vector3d;
import org.valkyrienskies.addon.control.ValkyrienSkiesControl;
import org.valkyrienskies.addon.control.block.BlockShipHelm;
import org.valkyrienskies.addon.control.block.multiblocks.TileEntityRudderPart;
import org.valkyrienskies.addon.control.nodenetwork.VSNode_TileEntity;
import org.valkyrienskies.mod.common.piloting.ControllerInputType;
import org.valkyrienskies.mod.common.piloting.PilotControlsMessage;
import org.valkyrienskies.mod.common.network.VSNetwork;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.tileentity.TileEntityPilotableImpl;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

import java.util.Optional;

public class TileEntityShipHelm extends TileEntityNodePilotableImpl implements ITickable {

    public double compassAngle = 0;
    public double lastCompassAngle = 0;

    public double wheelRotation = 0;
    public double lastWheelRotation = 0;

    private double nextWheelRotation;

    @Override
    public void update() {
        if (this.getWorld().isRemote) {
            calculateCompassAngle();
            lastWheelRotation = wheelRotation;
            wheelRotation += (nextWheelRotation - wheelRotation) * .25D;
        } else {
            // Only decay rotation when there's no pilot
            if (this.getPilotEntity() == null) {
                double friction = .05D;
                double toOriginRate = .05D;
                if (Math.abs(wheelRotation) < 1.5) {
                    wheelRotation = 0;
                } else {
                    // wheelRotation -= math.signum(wheelRotation) * wheelRotation;
                    double deltaForce = Math
                        .max(Math.abs(wheelRotation * toOriginRate) - friction, 0);
                    wheelRotation += deltaForce * -1 * Math.signum(wheelRotation);
                }
            }

            VSNode_TileEntity thisNode = this.getNode();

            for (GraphObject object : thisNode.getGraph().getObjects()) {
                VSNode_TileEntity otherNode = (VSNode_TileEntity) object;
                TileEntity tile = otherNode.getParentTile();
                if (tile instanceof TileEntityRudderPart) {
                    BlockPos masterPos = ((TileEntityRudderPart) tile).getMultiblockOrigin();
                    TileEntityRudderPart masterTile = (TileEntityRudderPart) tile.getWorld()
                        .getTileEntity(masterPos);
                    // This is a transient problem that only occurs during world loading.
                    if (masterTile != null) {
                        masterTile.setRudderAngle(-this.wheelRotation / 8D);
                    }
                }

            }

            VSNetwork.sendTileToAllNearby(this);
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        nextWheelRotation = pkt.getNbtCompound().getDouble("wheelRotation");
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tagToSend = new NBTTagCompound();
        tagToSend.setDouble("wheelRotation", wheelRotation);
        return new SPacketUpdateTileEntity(this.getPos(), 0, tagToSend);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound toReturn = super.getUpdateTag();
        toReturn.setDouble("wheelRotation", wheelRotation);
        return toReturn;
    }

    public void calculateCompassAngle() {
        lastCompassAngle = compassAngle;

        IBlockState helmState = getWorld().getBlockState(getPos());
        if (helmState.getBlock() != ValkyrienSkiesControl.INSTANCE.vsControlBlocks.shipHelm) {
            return;
        }
        EnumFacing enumfacing = helmState.getValue(BlockShipHelm.FACING);
        double wheelAndCompassStateRotation = enumfacing.getHorizontalAngle();

        BlockPos spawnPos = getWorld().getSpawnPoint();
        Vector3d compassPoint = new Vector3d(getPos().getX(), getPos().getY(), getPos().getZ());
        compassPoint.add(1D, 2D, 1D);

        Optional<PhysicsObject> physicsObject = ValkyrienUtils
            .getPhysoManagingBlock(getWorld(), getPos());
        if (physicsObject.isPresent()) {
            physicsObject.get()
                .getShipTransformationManager()
                .getCurrentTickTransform()
                .transformPosition(compassPoint, TransformType.SUBSPACE_TO_GLOBAL);
            // RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform,
            // compassPoint);
        }

        Vector3d compassDirection = new Vector3d(compassPoint);
        compassDirection.sub(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());

        if (physicsObject.isPresent()) {
            physicsObject.get()
                .getShipTransformationManager()
                .getCurrentTickTransform()
                .transformDirection(compassDirection, TransformType.GLOBAL_TO_SUBSPACE);
            // RotationMatrices.doRotationOnly(wrapper.wrapping.coordTransform.wToLTransform,
            // compassDirection);
        }

        compassDirection.normalize();
        compassAngle = Math.toDegrees(Math.atan2(compassDirection.x, compassDirection.z))
            - wheelAndCompassStateRotation;
        compassAngle = (compassAngle + 360D) % 360D;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        lastWheelRotation = wheelRotation = compound.getDouble("wheelRotation");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound toReturn = super.writeToNBT(compound);
        compound.setDouble("wheelRotation", wheelRotation);
        return toReturn;
    }

    @Override
    public ControllerInputType getControlInputType() {
        return ControllerInputType.ShipHelm;
    }

    @Override
    public void processControlMessage(PilotControlsMessage message, EntityPlayerMP sender) {
        double rotationDelta = 0;
        if (message.airshipLeft_KeyDown) {
            rotationDelta -= 12.5D;
        }
        if (message.airshipRight_KeyDown) {
            rotationDelta += 12.5D;
        }
        IBlockState blockState = this.getWorld().getBlockState(getPos());
        if (blockState.getBlock() instanceof BlockShipHelm) {
            EnumFacing facing = blockState.getValue(BlockShipHelm.FACING);
            if (this.isPlayerInFront(sender, facing)) {
                wheelRotation += rotationDelta;
            } else {
                wheelRotation -= rotationDelta;
            }
        }
        double max_rotation = 720D;
        wheelRotation = Math.min(Math.max(wheelRotation, -max_rotation), max_rotation);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderPilotText(FontRenderer renderer, ScaledResolution gameResolution) {
        // White text.
        int color = 0xFFFFFF;
        // Extra spaces so the that the text is closer to the middle when rendered.
        String message = "Wheel Rotation:    ";
        int i = gameResolution.getScaledWidth();
        int height = gameResolution.getScaledHeight() - 35;
        float middle = (float) (i / 2 - renderer.getStringWidth(message) / 2);
        message = "Wheel Rotation: " + Math.round(wheelRotation);
        renderer.drawStringWithShadow(message, middle, height, color);
    }

}
