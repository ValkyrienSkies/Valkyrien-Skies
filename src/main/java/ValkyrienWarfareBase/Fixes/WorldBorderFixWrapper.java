package ValkyrienWarfareBase.Fixes;

import ValkyrienWarfareBase.ChunkManagement.PhysicsChunkManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.border.EnumBorderStatus;
import net.minecraft.world.border.IBorderListener;
import net.minecraft.world.border.WorldBorder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * Wraps the WorldBorder object returned by WorldProvider to prevent conflicts between the border and PhysicsObjects
 *
 * @author thebest108
 */
public class WorldBorderFixWrapper extends WorldBorder {

    public final WorldBorder wrapping;

    public WorldBorderFixWrapper(WorldBorder toWrap) {
        wrapping = toWrap;
    }

    @Override
    public boolean contains(BlockPos pos) {
        if (PhysicsChunkManager.isLikelyShipChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
            return true;
        }
        return wrapping.contains(pos);
    }

    @Override
    public boolean contains(ChunkPos range) {
        if (PhysicsChunkManager.isLikelyShipChunk(range.x, range.z)) {
            return true;
        }
        return wrapping.contains(range);
    }

    @Override
    public boolean contains(AxisAlignedBB bb) {
        int xPos = (int) bb.minX;
        int zPos = (int) bb.minZ;
        if (PhysicsChunkManager.isLikelyShipChunk(xPos >> 4, zPos >> 4)) {
            return true;
        }
        return wrapping.contains(bb);
    }


    //Standard overrides past here
    @Override
    public double getClosestDistance(Entity entityIn) {
        return wrapping.getClosestDistance(entityIn);
    }

    @Override
    public double getClosestDistance(double x, double z) {
        return wrapping.getClosestDistance(x, z);
    }

    @Override
    public EnumBorderStatus getStatus() {
        return wrapping.getStatus();
    }

    @Override
    public double minX() {
        return wrapping.minX();
    }

    @Override
    public double minZ() {
        return wrapping.minZ();
    }

    @Override
    public double maxX() {
        return wrapping.maxX();
    }

    @Override
    public double maxZ() {
        return wrapping.maxZ();
    }

    @Override
    public double getCenterX() {
        return wrapping.getCenterX();
    }

    @Override
    public double getCenterZ() {
        return wrapping.getCenterZ();
    }

    @Override
    public void setCenter(double x, double z) {
        wrapping.setCenter(x, z);
    }

    @Override
    public double getDiameter() {
        return wrapping.getDiameter();
    }

    @Override
    public long getTimeUntilTarget() {
        return wrapping.getTimeUntilTarget();
    }

    @Override
    public double getTargetSize() {
        return wrapping.getTargetSize();
    }

    @Override
    public void setTransition(double newSize) {
        wrapping.setTransition(newSize);
    }

    @Override
    public void setTransition(double oldSize, double newSize, long time) {
        wrapping.setTransition(oldSize, newSize, time);
    }

    @Override
    public List<IBorderListener> getListeners() {
        return wrapping.getListeners();
    }

    @Override
    public void addListener(IBorderListener listener) {
        wrapping.addListener(listener);
    }

    @Override
    public int getSize() {
        return wrapping.getSize();
    }

    @Override
    public void setSize(int size) {
        wrapping.setSize(size);
    }

    @Override
    public double getDamageBuffer() {
        return wrapping.getDamageBuffer();
    }

    @Override
    public void setDamageBuffer(double bufferSize) {
        wrapping.setDamageBuffer(bufferSize);
    }

    @Override
    public double getDamageAmount() {
        return wrapping.getDamageAmount();
    }

    @Override
    public void setDamageAmount(double newAmount) {
        wrapping.setDamageAmount(newAmount);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getResizeSpeed() {
        return wrapping.getResizeSpeed();
    }

    @Override
    public int getWarningTime() {
        return wrapping.getWarningTime();
    }

    @Override
    public void setWarningTime(int warningTime) {
        wrapping.setWarningTime(warningTime);
    }

    @Override
    public int getWarningDistance() {
        return wrapping.getWarningDistance();
    }

    @Override
    public void setWarningDistance(int warningDistance) {
        wrapping.setWarningDistance(warningDistance);
    }

    @Override
    public void removeListener(IBorderListener listener) {
        wrapping.removeListener(listener);
    }

}
