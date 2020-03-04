package org.valkyrienskies.mod.common.physics.management.physo;

import org.junit.jupiter.api.Test;
import org.valkyrienskies.mod.common.ship_handling.ShipData;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ShipDataTest {

    @Test
    public void testNullPointer() {
        assertThrows(NullPointerException.class, () -> {
            ShipData shipData = ShipData.createData(null, null, null,
                null, null, null, null);
        });
    }

}
