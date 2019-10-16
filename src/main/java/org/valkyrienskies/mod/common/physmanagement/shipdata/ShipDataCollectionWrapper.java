package org.valkyrienskies.mod.common.physmanagement.shipdata;

import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * The sole purpose of this pathetic class is so that the root datatype of a Jackson serialized
 * data is NOT an array, which apparently is highly frowned upon
 */
@AllArgsConstructor
@NoArgsConstructor
class ShipDataCollectionWrapper {

    public Collection<ShipData> collection;

}
