package org.valkyrienskies.mod.common.physics;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.Chunk;
import org.joml.Matrix3d;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Vector3d;

public class TerrainRigidBody extends AbstractRigidBody {

    private final Chunk chunk;
    private final int yIndex;

    /**
     * Constructs a rigid body for terrain.
     *
     * @param controller The {@link ITransformController} that owns this rigid body
     */
    public TerrainRigidBody(ITransformController controller, Chunk chunk, int yIndex) {
        super(controller, ImmutableSet.of(), new InertiaData(new Vector3d(), new Matrix3d(), 0), new Matrix4d().translate(chunk.x * 16, yIndex * 16, chunk.z * 16), true);
        this.chunk = chunk;
        this.yIndex = yIndex;

        for (int x = 0; x < 16; x++) {
            for (int y = yIndex * 16; y < yIndex * 16 + 16; y++) {
                for (int z = 0; z < 16; z++) {
                    IBlockState state = chunk.getBlockState(x, y, z);
                    if (state != Blocks.AIR.getDefaultState()) {
                        Box box = new Box(new Vector3d(x + .5, y + .5, z + .5), new Vector3d(.5, .5, .5));
                        this.addBox(box);
                    }
                }
            }
        }
    }

    @Override
    protected void onTransformUpdate(Matrix4dc transform) {
        // Terrain can't move, so do nothing
    }
}
