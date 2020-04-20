package org.valkyrienskies.mod.common.util.datastructures;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.valkyrienskies.mod.common.util.datastructures.SmallBlockPosSet;

public class SmallBlockPosSetTest {

    @Test
    public void testDeHash() {
        SmallBlockPosSet set = new SmallBlockPosSet(10, -10);
        set.add(-329, 210, -2049);
        assertThat(set.iterator().next(), equalTo(new BlockPos(-329, 210, -2049)));
    }
}
