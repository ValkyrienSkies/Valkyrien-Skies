package org.valkyrienskies.mod.common.physics.bullet;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.valkyrienskies.mod.common.physics.PhysicsEngine;
import org.valkyrienskies.mod.common.physics.bullet.MeshCreator.Triangle;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;

public class BulletPhysicsEngine implements PhysicsEngine {

    private TIntObjectMap<BulletData> dataMap = new TIntObjectHashMap<>();

    @Override
    public void addPhysicsObject(@Nonnull PhysicsObject obj) {
        BulletData data = new BulletData();
        dataMap.put(obj.hashCode(), data);
        data.triangleList = MeshCreator.getMeshTriangles(obj.getBlockPositions());
    }

    @Nullable
    public BulletData getData(PhysicsObject obj) {
        return dataMap.get(obj.hashCode());
    }

    /**
     * Data used by the Bullet Physics Engine, uniquely assigned to each PhysicsObject
     */
    public class BulletData {
        public List<Triangle> triangleList;
    }

}
