package org.valkyrienskies.mod.common.command.autocompleters;

import java.util.Iterator;
import javax.annotation.Nonnull;
import net.minecraft.command.ICommandSender;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.physmanagement.shipdata.QueryableShipData;
import org.valkyrienskies.mod.common.physmanagement.shipdata.ShipData;

public class ShipNameAutocompleter implements Iterable<String> {

    Iterator<String> data;

    ShipNameAutocompleter(ICommandSender sender) {
        World world = sender.getEntityWorld();
        this.data = QueryableShipData.get(world).stream().map(ShipData::getName).iterator();
    }

    @Override
    @Nonnull
    public Iterator<String> iterator() {
        return data;
    }
}
