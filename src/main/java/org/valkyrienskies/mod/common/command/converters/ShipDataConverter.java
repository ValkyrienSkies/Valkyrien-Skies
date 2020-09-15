package org.valkyrienskies.mod.common.command.converters;

import lombok.RequiredArgsConstructor;
import net.minecraft.command.ICommandSender;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.ships.QueryableShipData;
import org.valkyrienskies.mod.common.ships.ShipData;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

@RequiredArgsConstructor
public class ShipDataConverter implements ITypeConverter<ShipData> {

    private final ICommandSender sender;

    @Override
    public ShipData convert(String value) throws TypeConversionException {
        World world = sender.getEntityWorld();
        QueryableShipData data = QueryableShipData.get(world);

        return data.getShipFromName(value)
            .orElseThrow(() -> new TypeConversionException("That ship, " + value + ", could not be found"));
    }
}
