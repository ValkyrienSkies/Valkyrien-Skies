package org.valkyrienskies.mod.common.entity;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.ships.ship_transform.CoordinateSpaceType;

public class EntityMountableChair extends EntityMountable {

    // Minecraft requires this constructor.
    @SuppressWarnings("unused")
    public EntityMountableChair(World worldIn) {
        super(worldIn);
    }

    public EntityMountableChair(World world, Vec3d mountPos,
        CoordinateSpaceType coordinateSpaceType, BlockPos chairPos) {
        super(world, mountPos, coordinateSpaceType, chairPos);
    }

    @Override
    public void onUpdate() {
        if (!getReferencePos().isPresent()) {
            // Some error occurred, kill this chair.
            new IllegalStateException("Chair mountable entity has no reference position.")
                .printStackTrace();
            this.setDead();
            return;
        }
        super.onUpdate();
    }
}
