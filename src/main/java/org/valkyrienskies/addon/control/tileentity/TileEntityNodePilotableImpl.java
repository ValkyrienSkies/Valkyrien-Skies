package org.valkyrienskies.addon.control.tileentity;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.joml.Vector3d;
import org.valkyrienskies.addon.control.nodenetwork.BasicNodeTileEntity;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.network.MessageStartPiloting;
import org.valkyrienskies.mod.common.network.MessageStopPiloting;
import org.valkyrienskies.mod.common.piloting.ControllerInputType;
import org.valkyrienskies.mod.common.piloting.ITileEntityPilotable;
import org.valkyrienskies.mod.common.piloting.PilotControlsMessage;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

/**
 * A basic implementation of the ITileEntityPilotable interface, other tile entities can extend this
 * for easy controls. This version comes with a built in node as well.
 */
public abstract class TileEntityNodePilotableImpl extends BasicNodeTileEntity implements
    ITileEntityPilotable {

    // Do NOT make this a reference to pilotPlayerEntity.
    @Nullable
    private UUID pilotPlayerEntity;

    public TileEntityNodePilotableImpl() {
        super();
        this.pilotPlayerEntity = null;
    }

    @Override
    public final void onPilotControlsMessage(PilotControlsMessage message, EntityPlayerMP sender) {
        if (sender.getUniqueID().equals(pilotPlayerEntity)) {
            processControlMessage(message, sender);
        }
    }

    @Override
    public final EntityPlayer getPilotEntity() {
        if (pilotPlayerEntity != null) {
            return getWorld().getPlayerEntityByUUID(pilotPlayerEntity);
        }
        return null;
    }

    @Override
    public final void setPilotEntity(EntityPlayer toSet) {
        if (!getWorld().isRemote) {
            EntityPlayer oldPlayer = getPilotEntity();
            sendPilotUpdatePackets((EntityPlayerMP) toSet, (EntityPlayerMP) oldPlayer);
        }
        if (toSet != null) {
            pilotPlayerEntity = toSet.getUniqueID();
            onStartTileUsage();
        } else {
            pilotPlayerEntity = null;
            onStopTileUsage();
        }
    }

    @Override
    public final void playerWantsToStopPiloting(EntityPlayer player) {
        if (player == getPilotEntity()) {
            setPilotEntity(null);
        }
    }

    @Override
    public final PhysicsObject getParentPhysicsEntity() {
        Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysoManagingBlock(world, pos);
        if (physicsObject.isPresent()) {
            return physicsObject.get();
        } else {
            return null;
        }
    }

    // Always call this before setting the pilotPlayerEntity to equal newPilot
    private void sendPilotUpdatePackets(EntityPlayerMP newPilot, EntityPlayerMP oldPilot) {
        // If old pilot equals new pilot, then don't send the stop piloting message
        if (oldPilot != null && oldPilot != newPilot) {
            MessageStopPiloting stopMessage = new MessageStopPiloting(getPos());
            ValkyrienSkiesMod.controlNetwork.sendTo(stopMessage, oldPilot);
        }
        if (newPilot != null) {
            MessageStartPiloting startMessage = new MessageStartPiloting(getPos(),
                setClientPilotingEntireShip(),
                getControlInputType());
            ValkyrienSkiesMod.controlNetwork.sendTo(startMessage, newPilot);
        }
    }

    /**
     * Unique for each tileentity type
     *
     * @return
     */
    public abstract ControllerInputType getControlInputType();

    /**
     * Returns true if this control type is piloting the ship.
     *
     * @return
     */
    @Deprecated
    public boolean setClientPilotingEntireShip() {
        return false;
    }

    /**
     * Unique for each tileentity type, only called if the sender player is the same as the
     * pilotPlayerEntity
     *
     * @return
     */
    public abstract void processControlMessage(PilotControlsMessage message, EntityPlayerMP sender);

    /**
     * @param player
     * @param blockFacing
     * @return true if the passed player is in front of the given blockFacing, false if not.
     */
    protected boolean isPlayerInFront(EntityPlayer player, EnumFacing blockFacing) {
        Vector3d tileRelativePos = new Vector3d(this.getPos().getX() + .5, this.getPos().getY() + .5,
            this.getPos().getZ() + .5);
        if (this.getParentPhysicsEntity() != null) {
            this.getParentPhysicsEntity().getShipTransformationManager()
                .getCurrentTickTransform()
                .transformPosition(tileRelativePos, TransformType.SUBSPACE_TO_GLOBAL);
        }
        tileRelativePos.sub(player.posX, player.posY, player.posZ);
        Vector3d normal = new Vector3d(blockFacing.getDirectionVec().getX() * -1,
            blockFacing.getDirectionVec().getY(),
            blockFacing.getDirectionVec().getZ());

        if (this.getParentPhysicsEntity() != null) {
            this.getParentPhysicsEntity().getShipTransformationManager()
                .getCurrentTickTransform()
                .transformDirection(normal, TransformType.SUBSPACE_TO_GLOBAL);
        }

        double dotProduct = tileRelativePos.dot(normal);
        return dotProduct > 0;
    }

}
