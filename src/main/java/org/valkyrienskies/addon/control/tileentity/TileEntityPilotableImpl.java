package org.valkyrienskies.addon.control.tileentity;

import java.util.Optional;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.valkyrienskies.addon.control.ValkyrienSkiesControl;
import org.valkyrienskies.addon.control.network.MessageStartPiloting;
import org.valkyrienskies.addon.control.network.MessageStopPiloting;
import org.valkyrienskies.addon.control.nodenetwork.BasicNodeTileEntity;
import org.valkyrienskies.addon.control.piloting.ControllerInputType;
import org.valkyrienskies.addon.control.piloting.ITileEntityPilotable;
import org.valkyrienskies.addon.control.piloting.PilotControlsMessage;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

/**
 * A basic implementation of the ITileEntityPilotable interface, other tile entities can extend this
 * for easy controls
 *
 * @author thebest108
 */
public abstract class TileEntityPilotableImpl extends BasicNodeTileEntity implements
    ITileEntityPilotable {

    private EntityPlayer pilotPlayerEntity;

    TileEntityPilotableImpl() {
        super();
        this.pilotPlayerEntity = null;
    }

    @Override
    public final void onPilotControlsMessage(PilotControlsMessage message, EntityPlayerMP sender) {
        if (sender == pilotPlayerEntity) {
            processControlMessage(message, sender);
        } else {
            // Wtf is this packet being sent for?
        }
    }

    @Override
    public final EntityPlayer getPilotEntity() {
        return pilotPlayerEntity;
    }

    @Override
    public final void setPilotEntity(EntityPlayer toSet) {
        if (!getWorld().isRemote) {
            sendPilotUpdatePackets((EntityPlayerMP) toSet, (EntityPlayerMP) pilotPlayerEntity);
        }
        pilotPlayerEntity = toSet;
        if (pilotPlayerEntity != null) {
            onStartTileUsage(pilotPlayerEntity);
        } else {
            onStopTileUsage();
        }
    }

    @Override
    public final void playerWantsToStopPiloting(EntityPlayer player) {
        if (player == getPilotEntity()) {
            setPilotEntity(null);
        } else {
            // Wtf happened here?
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
    private final void sendPilotUpdatePackets(EntityPlayerMP newPilot, EntityPlayerMP oldPilot) {
        if (oldPilot != null) {
            MessageStopPiloting stopMessage = new MessageStopPiloting(getPos());
            ValkyrienSkiesControl.controlNetwork.sendTo(stopMessage, oldPilot);
        }
        if (newPilot != null) {
            MessageStartPiloting startMessage = new MessageStartPiloting(getPos(),
                setClientPilotingEntireShip(),
                getControlInputType());
            ValkyrienSkiesControl.controlNetwork.sendTo(startMessage, newPilot);
        }
    }

    /**
     * Unique for each tileentity type
     *
     * @return
     */
    abstract ControllerInputType getControlInputType();

    /**
     * Returns true if this control type is piloting the ship.
     *
     * @return
     */
    @Deprecated
    boolean setClientPilotingEntireShip() {
        return false;
    }

    /**
     * Unique for each tileentity type, only called if the sender player is the same as the
     * pilotPlayerEntity
     *
     * @return
     */
    abstract void processControlMessage(PilotControlsMessage message, EntityPlayerMP sender);

    /**
     * @param player
     * @param blockFacing
     * @return true if the passed player is in front of the given blockFacing, false if not.
     */
    protected boolean isPlayerInFront(EntityPlayer player, EnumFacing blockFacing) {
        Vector tileRelativePos = new Vector(this.getPos().getX() + .5, this.getPos().getY() + .5,
            this.getPos().getZ() + .5);
        if (this.getParentPhysicsEntity() != null) {
            this.getParentPhysicsEntity().getShipTransformationManager()
                .getCurrentTickTransform()
                .transform(tileRelativePos, TransformType.SUBSPACE_TO_GLOBAL);
        }
        tileRelativePos.subtract(player.posX, player.posY, player.posZ);
        Vector normal = new Vector(blockFacing.getDirectionVec().getX() * -1,
            blockFacing.getDirectionVec().getY(),
            blockFacing.getDirectionVec().getZ());

        if (this.getParentPhysicsEntity() != null) {
            this.getParentPhysicsEntity().getShipTransformationManager()
                .getCurrentTickTransform()
                .rotate(normal, TransformType.SUBSPACE_TO_GLOBAL);
        }

        double dotProduct = tileRelativePos.dot(normal);
        return dotProduct > 0;
    }

    /**
     * This is called during the post render of every frame in Minecraft. Override this to allow a
     * pilot tileentity to display info as text on the screen.
     *
     * @param renderer
     * @param gameResolution
     */
    @SideOnly(Side.CLIENT)
    public void renderPilotText(FontRenderer renderer, ScaledResolution gameResolution) {

    }
}
