package org.valkyrienskies.mod.common.command.autocompleters;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Iterator;
import javax.annotation.Nonnull;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.common.DimensionManager;

public class WorldAutocompleter implements Iterable<String> {

    ImmutableList<String> worldNames;

    WorldAutocompleter(ICommandSender sender) {
        worldNames = Arrays.stream(DimensionManager.getWorlds())
            .map(world -> world.provider.getDimensionType().getName())
            .collect(ImmutableList.toImmutableList());
    }

    @Nonnull
    @Override
    public Iterator<String> iterator() {
        return worldNames.iterator();
    }
}
