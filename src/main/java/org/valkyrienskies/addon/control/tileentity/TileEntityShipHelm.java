/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.valkyrienskies.addon.control.ValkyrienSkiesControl;
import org.valkyrienskies.addon.control.block.BlockShipHelm;
import org.valkyrienskies.addon.control.block.multiblocks.TileEntityRudderAxlePart;
import org.valkyrienskies.addon.control.nodenetwork.VWNode_TileEntity;
import org.valkyrienskies.addon.control.piloting.ControllerInputType;
import org.valkyrienskies.addon.control.piloting.PilotControlsMessage;
import org.valkyrienskies.fixes.VWNetwork;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

public class TileEntityShipHelm extends TileEntityPilotableImpl implements ITickable {

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

            VWNode_TileEntity thisNode = this.getNode();

            for (GraphObject object : thisNode.getGraph().getObjects()) {
                VWNode_TileEntity otherNode = (VWNode_TileEntity) object;
                TileEntity tile = otherNode.getParentTile();
                if (tile instanceof TileEntityRudderAxlePart) {
                    BlockPos masterPos = ((TileEntityRudderAxlePart) tile).getMultiblockOrigin();
                    TileEntityRudderAxlePart masterTile = (TileEntityRudderAxlePart) tile.getWorld()
                        .getTileEntity(masterPos);
                    // This is a transient problem that only occurs during world loading.
                    if (masterTile != null) {
                        masterTile.setRudderAngle(-this.wheelRotation / 8D);
                    }
                }

            }

            VWNetwork.sendTileToAllNearby(this);
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
        if (helmState.getBlock() != ValkyrienSkiesControl.INSTANCE.vwControlBlocks.shipHelm) {
            return;
        }
        EnumFacing enumfacing = helmState.getValue(BlockShipHelm.FACING);
        double wheelAndCompassStateRotation = enumfacing.getHorizontalAngle();

        BlockPos spawnPos = getWorld().getSpawnPoint();
        Vector compassPoint = new Vector(getPos().getX(), getPos().getY(), getPos().getZ());
        compassPoint.add(1D, 2D, 1D);

        Optional<PhysicsObject> physicsObject = ValkyrienUtils
            .getPhysicsObject(getWorld(), getPos());
        if (physicsObject.isPresent()) {
            physicsObject.get()
                .getShipTransformationManager()
                .getCurrentTickTransform()
                .transform(compassPoint,
                    TransformType.SUBSPACE_TO_GLOBAL);
            // RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform,
            // compassPoint);
        }

        Vector compassDirection = new Vector(compassPoint);
        compassDirection.subtract(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());

        if (physicsObject.isPresent()) {
            physicsObject.get()
                .getShipTransformationManager()
                .getCurrentTickTransform()
                .rotate(compassDirection,
                    TransformType.GLOBAL_TO_SUBSPACE);
            // RotationMatrices.doRotationOnly(wrapper.wrapping.coordTransform.wToLTransform,
            // compassDirection);
        }

        compassDirection.normalize();
        compassAngle = Math.toDegrees(Math.atan2(compassDirection.X, compassDirection.Z))
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
    ControllerInputType getControlInputType() {
        return ControllerInputType.ShipHelm;
    }

    @Override
    void processControlMessage(PilotControlsMessage message, EntityPlayerMP sender) {
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
