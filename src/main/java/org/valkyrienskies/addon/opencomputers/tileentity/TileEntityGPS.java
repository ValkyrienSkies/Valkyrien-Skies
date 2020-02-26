package org.valkyrienskies.addon.opencomputers.tileentity;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Optional;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")
public class TileEntityGPS extends TileEntity implements SimpleComponent {

    public TileEntityGPS() {
        super();
    }

    // Used by OpenComputers
    @Override
    public String getComponentName() {
        return "ship_gps";
    }

    @Callback
    @Optional.Method(modid = "opencomputers")
    public Object[] getPosition(Context context, Arguments args) {
        java.util.Optional<PhysicsObject> physicsObjectOptional = ValkyrienUtils
            .getPhysoManagingBlock(getWorld(), getPos());
        if (physicsObjectOptional.isPresent()) {
            BlockPos pos = physicsObjectOptional.get().getShipTransform().getShipPositionBlockPos();
            return new Object[]{pos.getX(), pos.getY(), pos.getZ()};
        }
        return null;
    }

    @Callback
    @Optional.Method(modid = "opencomputers")
    public Object[] getRotation(Context context, Arguments args) {
        java.util.Optional<PhysicsObject> physicsObjectOptional = ValkyrienUtils
            .getPhysoManagingBlock(getWorld(), getPos());
        if (physicsObjectOptional.isPresent()) {
            ShipTransform transform = physicsObjectOptional.get().getShipTransform();
            Vector3dc angles = transform.getSubspaceToGlobal().getEulerAnglesZYX(new Vector3d());
            return new Object[]{Math.toDegrees(angles.x()), Math.toDegrees(angles.y()), Math.toDegrees(angles.z())};
        }
        return null;
    }
}
