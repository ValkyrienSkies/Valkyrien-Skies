package org.valkyrienskies.mod.common.command.autocompleters;

import java.util.Iterator;
import javax.annotation.Nonnull;
import net.minecraft.command.ICommandSender;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.physics.management.physo.PhysoData;
import org.valkyrienskies.mod.common.physmanagement.shipdata.QueryableShipData;

public class ShipNameAutocompleter implements Iterable<String> {

    Iterator<String> data;

    ShipNameAutocompleter(ICommandSender sender) {
        World world = sender.getEntityWorld();
        this.data = QueryableShipData.get(world).stream().map(PhysoData::getName).iterator();
    }

    @Override
    @Nonnull
    public Iterator<String> iterator() {
        return data;
    }
}
