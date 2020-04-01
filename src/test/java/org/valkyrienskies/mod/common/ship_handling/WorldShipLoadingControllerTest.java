package org.valkyrienskies.mod.common.ship_handling;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.valkyrienskies.mod.common.ship_handling.WorldShipLoadingController.*;

public class WorldShipLoadingControllerTest {

    /**
     * These constants must satisfy the following constraints:
     *
     * WATCH_DISTANCE < UNWATCH_DISTANCE
     * LOAD_DISTANCE < LOAD_BACKGROUND_DISTANCE < UNLOAD_DISTANCE
     * UNWATCH_DISTANCE <= UNLOAD_DISTANCE
     * WATCH_DISTANCE <= LOAD_DISTANCE
     */
    @Test
    public void testLoadingConstants() {
        assertTrue(WATCH_DISTANCE < UNWATCH_DISTANCE);
        assertTrue(LOAD_DISTANCE < LOAD_BACKGROUND_DISTANCE);
        assertTrue(LOAD_BACKGROUND_DISTANCE < UNLOAD_DISTANCE);
        assertTrue(UNWATCH_DISTANCE <= UNLOAD_DISTANCE);
        assertTrue(WATCH_DISTANCE <= LOAD_DISTANCE);
    }
}
