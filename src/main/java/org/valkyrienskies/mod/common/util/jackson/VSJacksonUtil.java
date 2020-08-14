package org.valkyrienskies.mod.common.util.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import org.valkyrienskies.mod.common.ships.ShipData;
import org.valkyrienskies.mod.common.util.jackson.annotations.VSAnnotationIntrospector;

public class VSJacksonUtil {

    private static CBORMapper defaultMapper;
    private static CBORMapper packetMapper;

    /**
     * Returns the default mapper for the standard Valkyrien Skies configuration * for serializing
     * things, particularly {@link ShipData}
     */
    public static ObjectMapper getDefaultMapper() {
        if (defaultMapper == null) {
            CBORMapper mapper = new CBORMapper();
            configureMapper(mapper);
            defaultMapper = mapper;
        }
        return defaultMapper;
    }

    /**
     * Returns the default mapper for Valkyrien Skies network transmissions (e.g., it ignores
     * {@link org.valkyrienskies.mod.common.util.jackson.annotations.PacketIgnore} annotated fields
     */
    public static ObjectMapper getPacketMapper() {
        if (packetMapper == null) {
            CBORMapper mapper = new CBORMapper();
            configurePacketMapper(mapper);
            packetMapper = mapper;
        }
        return packetMapper;
    }

    public static void configurePacketMapper(ObjectMapper mapper) {
        configureMapper(mapper);

        mapper.setAnnotationIntrospector(VSAnnotationIntrospector.instance);
    }

    /**
     * Configures the selected object mapper to use the standard Valkyrien Skies configuration for
     * serializing things, particularly {@link ShipData}
     *
     * @param mapper The ObjectMapper to configure
     */
    public static void configureMapper(ObjectMapper mapper) {
        mapper.registerModule(new MinecraftSerializationModule())
            .registerModule(new JOMLSerializationModule())
            .setVisibility(mapper.getVisibilityChecker()
                .withFieldVisibility(Visibility.ANY)
                .withGetterVisibility(Visibility.NONE)
                .withIsGetterVisibility(Visibility.NONE)
                .withSetterVisibility(Visibility.NONE));
    }

}
