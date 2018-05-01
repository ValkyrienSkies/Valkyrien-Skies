package valkyrienwarfare.addon.opencomputers.tileentity;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Optional;
import valkyrienwarfare.api.ValkyrienWarfareHooks;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

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
        if (ValkyrienWarfareHooks.isBlockPartOfShip(world, pos))
        {
            BlockPos pos = ValkyrienWarfareHooks.getShipEntityManagingPos(getWorld(), getPos()).getPosition();
            return new Object[]{ pos.getX(), pos.getY(), pos.getZ() };
        }
        return null;
    }

    @Callback
    @Optional.Method(modid = "opencomputers")
    public Object[] getRotation(Context context, Arguments args) {
        if (ValkyrienWarfareHooks.isBlockPartOfShip(world, pos))
        {
            PhysicsWrapperEntity ship = ValkyrienWarfareHooks.getShipEntityManagingPos(getWorld(), getPos());
            return new Object[]{ ship.yaw, ship.pitch, ship.roll };
        }
        return null;
    }
}
