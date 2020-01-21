package org.valkyrienskies.mod.common.physics;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import org.joml.Matrix3d;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.physmanagement.shipdata.IBlockPosSet;
import org.valkyrienskies.mod.common.physmanagement.shipdata.NaiveBlockPosSet;
import org.valkyrienskies.mod.common.util.JOML;

public class TerrainRigidBody extends AbstractRigidBody {

    private final Chunk chunk;
    private final int yIndex;

    /**
     * Constructs a rigid body for terrain.
     *
     * @param controller The {@link ITransformController} that owns this rigid body
     */
    public TerrainRigidBody(ITransformController controller, Chunk chunk, int yIndex) {
        super(controller, ImmutableSet.of(), new InertiaData(new Vector3d(), new Matrix3d(), 0),
            new Matrix4d().translate(chunk.x * 16 + 8, yIndex * 16 + 8, chunk.z * 16 + 8), true);
        this.chunk = chunk;
        this.yIndex = yIndex;

        IBlockPosSet positions = new NaiveBlockPosSet();
        Vector3dc halfExtents = new Vector3d(.5, .5, .5);

        for (int x = 0; x < 16; x++) {
            for (int y = yIndex * 16; y < yIndex * 16 + 16; y++) {
                for (int z = 0; z < 16; z++) {
                    IBlockState state = chunk.getBlockState(x, y, z);
                    if (state != Blocks.AIR.getDefaultState()) {
                        positions.add(x, y, z);
                    }
                }
            }
        }

        for (BlockPos pos : positions) {
            if (!positions.isSurrounded(pos)) {
                Box box = new Box(JOML.convertDouble(pos).add(0.5 - 8, 0.5 - (yIndex * 16) - 8, 0.5 - 8), halfExtents);
                this.addBox(box);
            }
        }
    }

    @Override
    protected void onTransformUpdate(Matrix4dc transform) {
        // Terrain can't move, so do nothing
    }
}
