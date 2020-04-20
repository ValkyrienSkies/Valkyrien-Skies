package org.valkyrienskies.mod.common.ships;

import org.junit.jupiter.api.Test;
import org.valkyrienskies.mod.common.ships.ShipData;

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
