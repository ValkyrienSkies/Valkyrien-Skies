package org.valkyrienskies.mod.common.physics;

import com.badlogic.gdx.math.Matrix4;
import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import lombok.Value;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import org.joml.*;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;

import java.lang.Math;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ShipRigidBody extends AbstractRigidBody {

    @Getter
    private final Vector3dc referencePos;
    private final Map<BlockPos, ImmutableSet<Box>> blockposToBoxes;

    /**
     *
     * @param physicsObject
     * @param controller
     */
    public ShipRigidBody(PhysicsObject physicsObject, ITransformController controller) {
        super(controller, ImmutableSet.of(), generateInitialForShip(physicsObject), generateInitialTransform(physicsObject), false);
        BlockPos refBlockPos = physicsObject.getReferenceBlockPos();
        this.referencePos = new Vector3d(refBlockPos.getX(), refBlockPos.getY(), refBlockPos.getZ());
        this.blockposToBoxes = new HashMap<>();

        for (BlockPos pos : physicsObject.getData().getBlockPositions()) {
            IBlockState state = physicsObject.getWorld().getBlockState(pos);
            updateBlock(pos, Blocks.AIR.defaultBlockState, state);
        }
    }

    public void updateBlock(BlockPos pos, IBlockState oldState, IBlockState newState) {
        ImmutableSet<Box> removed = blockposToBoxes.get(pos);
        ImmutableSet<Box> added = generateBoxesForBlock(pos, newState);
        // Remove/Add boxes from the shape
        this.updateShape(added, removed);
        // Update the BlockPos to boxes map.
        if (added != null) {
            blockposToBoxes.put(pos, added);
        } else {
            blockposToBoxes.remove(pos);
        }
    }

    private ImmutableSet<Box> generateBoxesForBlock(BlockPos pos, IBlockState state) {
        if (state == Blocks.AIR.getDefaultState()) {
            return ImmutableSet.of();
        }
        Vector3dc halfExtends = new Vector3d(.5, .5, .5);
        Vector3dc center = new Vector3d(pos.getX() + .5 - referencePos.x(), pos.getY() + .5 - referencePos.y(), pos.getZ() + .5 - referencePos.z());
        Box box = new Box(center, halfExtends);
        return ImmutableSet.of(box);
    }

    public static InertiaData generateInitialForShip(PhysicsObject physicsObject) {
        BlockPos referencePos = physicsObject.getReferenceBlockPos();

        // TODO: Temp
        InertiaData data = new InertiaData(physicsObject.getInertiaData().getGameTickCenterOfMass().toVector3d(), new Matrix3d(), (float) physicsObject.getInertiaData().getGameTickMass());
        return data;
    }

    private static Matrix4dc generateInitialTransform(PhysicsObject physicsObject) {
        ShipTransform shipTransform = physicsObject.getShipTransformationManager().getCurrentTickTransform();

        return new Matrix4d()
                .translate(shipTransform.getPosX(), shipTransform.getPosY(), shipTransform.getPosZ())
                .rotateXYZ(Math.toRadians(shipTransform.getPitch()), Math.toRadians(shipTransform.getYaw()), Math.toRadians(shipTransform.getRoll()));
    }

    @Override
    protected void onTransformUpdate(Matrix4dc transform) {
        System.out.println("lmao");
    }
}
