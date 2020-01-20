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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ShipRigidBody extends AbstractRigidBody {

    private final BlockPos referencePos;
    private final Map<BlockPos, ImmutableSet<Box>> blockposToBoxes;

    /**
     *
     * @param physicsObject
     * @param controller
     */
    public ShipRigidBody(PhysicsObject physicsObject, ITransformController controller) {
        super(controller, ImmutableSet.of(), generateInitialForShip(physicsObject), generateInitialTransform(physicsObject), false);
        this.referencePos = physicsObject.getReferenceBlockPos();
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
        Vector3dc center = new Vector3d(pos.getX() - referencePos.getX() + .5, pos.getY() - referencePos.getY() + .5, pos.getZ() - referencePos.getZ() + .5);
        Box box = new Box(center, halfExtends);
        return ImmutableSet.of(box);
    }

    private static InertiaData generateInitialForShip(PhysicsObject physicsObject) {
        BlockPos referencePos = physicsObject.getReferenceBlockPos();
        Vector3dc rigidBodyCenter = physicsObject.getCenterCoord().toVector3d().sub(referencePos.getX(), referencePos.getY(), referencePos.getZ(), new Vector3d());


        // TODO: Temp
        InertiaData data = new InertiaData(physicsObject.getCenterCoord().toVector3d(), new Matrix3d(), 1000);
        return data;
    }

    private static Matrix4dc generateInitialTransform(PhysicsObject physicsObject) {
        ShipTransform shipTransform = physicsObject.getShipTransformationManager().getCurrentTickTransform();

        Matrix4dc rotate = new Matrix4d().rotateXYZ(shipTransform.getPitch(), shipTransform.getYaw(), shipTransform.getRoll());
        Matrix4dc translate = new Matrix4d().translate(shipTransform.getPosX(), shipTransform.getPosY(), shipTransform.getPosZ());

        // TODO: I think this is right?
        return translate.mul(rotate, new Matrix4d());
    }

    @Override
    protected void onTransformUpdate(Matrix4dc transform) {
        System.out.println("lmao");
    }
}
