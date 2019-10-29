package org.valkyrienskies.mod.common.physics.management.physo;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ShipDataTest {

    @Test
    public void testNullPointer() {
        assertThrows(NullPointerException.class, () -> {
            ShipData shipData = ShipData.createData(null, null, null,
                null, null, null, null);
        });
    }

}
