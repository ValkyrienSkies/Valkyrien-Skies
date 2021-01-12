package org.valkyrienskies.mod.common.command.converters;

import net.minecraft.util.math.Vec3d;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

public class Vec3dDataConverter implements ITypeConverter<Vec3d> {

    private static final String DOUBLE_REGEX = "-?(\\d*\\.\\d+)|(\\d+\\.)|(\\d+)"; // Regex for doubles
    private static final String VECTOR_REGEX = String.format("<%s,%s,%s>", DOUBLE_REGEX, DOUBLE_REGEX, DOUBLE_REGEX);

    @Override
    public Vec3d convert(final String value) throws TypeConversionException {
        try {
            if (!value.matches(VECTOR_REGEX)) {
                // Remove the '<' '>' from the value string
                final String trimmedValue = value.substring(1, value.length() - 1);
                final String[] doublesInValue = trimmedValue.split(",");

                final double posX = Double.parseDouble(doublesInValue[0]);
                final double posY = Double.parseDouble(doublesInValue[1]);
                final double posZ = Double.parseDouble(doublesInValue[2]);

                return new Vec3d(posX, posY, posZ);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        throw new TypeConversionException("Cannot convert " + value + " to a vector");
    }
}
