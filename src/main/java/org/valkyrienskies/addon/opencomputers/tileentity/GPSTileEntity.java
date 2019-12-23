package org.valkyrienskies.addon.opencomputers.tileentity;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Optional;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;
import org.valkyrienskies.mod.common.physics.management.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")
public class GPSTileEntity extends TileEntity implements SimpleComponent {

    public GPSTileEntity() {
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
            .getPhysicsObject(getWorld(), getPos());
        if (physicsObjectOptional.isPresent()) {
            BlockPos pos = physicsObjectOptional.get()
                .getWrapperEntity()
                .getPosition();
            return new Object[]{pos.getX(), pos.getY(), pos.getZ()};
        }
        return null;
    }

    @Callback
    @Optional.Method(modid = "opencomputers")
    public Object[] getRotation(Context context, Arguments args) {
        java.util.Optional<PhysicsObject> physicsObjectOptional = ValkyrienUtils
            .getPhysicsObject(getWorld(), getPos());
        if (physicsObjectOptional.isPresent()) {
            PhysicsWrapperEntity ship = physicsObjectOptional.get()
                .getWrapperEntity();
            return new Object[]{ship.getYaw(), ship.getPitch(), ship.getRoll()};
        }
        return null;
    }
}
