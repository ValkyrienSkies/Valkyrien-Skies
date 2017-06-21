package ValkyrienWarfareControl.TileEntity;

import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareControl.Block.BlockShipHelm;
import ValkyrienWarfareControl.Network.PlayerUsingControlsMessage;
import ValkyrienWarfareControl.ValkyrienWarfareControlMod;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public class TileEntityShipHelm extends TileEntity implements ITickable {

    public double compassAngle = 0;
    public double lastCompassAngle = 0;

    public double wheelRotation = 0;
    public double lastWheelRotation = 0;

    public EntityPlayer operator;

    public void onRightClicked(EntityPlayer player) {
        if (operator == null) {
            mountPlayerOntoWheel(player);
        }
    }

    private void mountPlayerOntoWheel(EntityPlayer player) {
        operator = player;

        PlayerUsingControlsMessage message = new PlayerUsingControlsMessage(player, this.getPos(), true);
        ValkyrienWarfareControlMod.controlNetwork.sendToAllAround(message, new TargetPoint(this.getWorld().provider.getDimension(), player.posX, player.posY, player.posZ, 128D));
    }

    @Override
    public void update() {
        if (this.getWorld().isRemote) {
            calculateCompassAngle();
            lastWheelRotation = wheelRotation;
        } else {
            double distanceToZero = -wheelRotation;

            wheelRotation += distanceToZero / 10D;

            SPacketUpdateTileEntity spacketupdatetileentity = getUpdatePacket();
            WorldServer serverWorld = (WorldServer) world;
            Vector pos = new Vector(getPos().getX(), getPos().getY(), getPos().getZ());
            PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(getWorld(), getPos());
            if (wrapper != null) {
                RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, pos);
            }
            serverWorld.mcServer.getPlayerList().sendToAllNearExcept(null, pos.X, pos.Y, pos.Z, 128D, getWorld().provider.getDimension(), spacketupdatetileentity);
        }
    }

    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.SPacketUpdateTileEntity pkt) {
//		lastWheelRotation = wheelRotation;

        wheelRotation = pkt.getNbtCompound().getDouble("wheelRotation");

    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tagToSend = new NBTTagCompound();
        tagToSend.setDouble("wheelRotation", wheelRotation);
        return new SPacketUpdateTileEntity(this.getPos(), 0, tagToSend);
    }

    public void calculateCompassAngle() {
        lastCompassAngle = compassAngle;

        IBlockState helmState = getWorld().getBlockState(getPos());
        EnumFacing enumfacing = (EnumFacing) helmState.getValue(BlockShipHelm.FACING);
        double wheelAndCompassStateRotation = enumfacing.getHorizontalAngle();

        BlockPos spawnPos = getWorld().getSpawnPoint();
        Vector compassPoint = new Vector(getPos().getX(), getPos().getY(), getPos().getZ());
        compassPoint.add(1D, 2, 1D);

        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(getWorld(), getPos());
        if (wrapper != null) {
            RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, compassPoint);
        }

        Vector compassDirection = new Vector(compassPoint);
        compassDirection.subtract(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());

        if (wrapper != null) {
            RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLRotation, compassDirection);
        }

        compassDirection.normalize();
        compassAngle = Math.toDegrees(Math.atan2(compassDirection.X, compassDirection.Z)) - wheelAndCompassStateRotation;
        compassAngle = (compassAngle + 360D) % 360D;
    }

    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        lastWheelRotation = wheelRotation = compound.getDouble("wheelRotation");
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound toReturn = super.writeToNBT(compound);

        compound.setDouble("wheelRotation", wheelRotation);

        return toReturn;
    }

}
