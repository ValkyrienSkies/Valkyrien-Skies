package org.valkyrienskies.mod.common.capability;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.valkyrienskies.mod.common.capability.framework.VSDefaultCapability;
import org.valkyrienskies.mod.common.ships.ship_world.VSWorldData;

/**
 * This sort of class basically only exists because Java generics are trash
 */
public class VSWorldDataCapability extends VSDefaultCapability<VSWorldData> {

    public VSWorldDataCapability(ObjectMapper mapper) {
        super(VSWorldData.class, VSWorldData::new, mapper);
    }

    public VSWorldDataCapability() {
        super(VSWorldData.class, VSWorldData::new);
    }

}
