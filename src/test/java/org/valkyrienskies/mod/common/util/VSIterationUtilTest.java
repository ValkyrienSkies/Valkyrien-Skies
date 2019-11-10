package org.valkyrienskies.mod.common.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.joml.Vector2i;
import org.joml.Vector3i;
import org.junit.jupiter.api.Test;

public class VSIterationUtilTest {

    @Test
    public void testIterator2d() {
        Iterator<Vector2i> iterator = new VSIterationUtil.Int2dIterator(-10, -10, 10, 10);

        List<Vector2i> iVals = new ArrayList<>();
        List<Vector2i> oVals = new ArrayList<>();

        while (iterator.hasNext()) {
            Vector2i vec = iterator.next();
            iVals.add(vec);
        }

        VSIterationUtil.iterate2d(-10, -10,  10, 10, (x, y) -> oVals.add(new Vector2i(x, y)));

        assertThat(iVals, containsInAnyOrder(oVals.toArray()));
    }

    @Test
    public void testIterator3d() {
        Iterator<Vector3i> iterator = new VSIterationUtil.Int3dIterator(-10, -10, -10, 10, 10, 10);

        List<Vector3i> iVals = new ArrayList<>();
        List<Vector3i> oVals = new ArrayList<>();

        while (iterator.hasNext()) {
            Vector3i vec = iterator.next();
            iVals.add(vec);
        }

        VSIterationUtil.iterate3d(-10, -10, -10, 10, 10, 10,
            (x, y, z) -> oVals.add(new Vector3i(x, y, z)));

        assertThat(iVals, containsInAnyOrder(oVals.toArray()));
    }

}
