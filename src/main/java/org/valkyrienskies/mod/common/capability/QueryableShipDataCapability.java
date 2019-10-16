package org.valkyrienskies.mod.common.capability;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.valkyrienskies.mod.common.capability.framework.VSDefaultCapability;
import org.valkyrienskies.mod.common.physmanagement.shipdata.QueryableShipData;

/**
 * This sort of class basically only exists because Java generics are trash
 */
public class QueryableShipDataCapability extends VSDefaultCapability<QueryableShipData> {

    public QueryableShipDataCapability(ObjectMapper mapper) {
        super(QueryableShipData.class, QueryableShipData::new, mapper);
    }

    public QueryableShipDataCapability() {
        super(QueryableShipData.class, QueryableShipData::new);
    }

}
