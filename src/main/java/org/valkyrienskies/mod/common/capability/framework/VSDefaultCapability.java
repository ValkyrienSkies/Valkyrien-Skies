package org.valkyrienskies.mod.common.capability.framework;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import org.valkyrienskies.mod.common.capability.VSWorldDataCapability;
import org.valkyrienskies.mod.common.network.AABBMixinSerializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.function.Supplier;

/**
 * Implement as follows
 *
 * <pre>{@code
 * public class VSWorldDataCapability extends VSDefaultCapability<VSWorldData> {
 *     public VSWorldDataCapability(ObjectMapper mapper) {
 *         super(VSWorldData.class, VSWorldData::new, mapper);
 *     }
 *
 *     public VSWorldDataCapability() {
 *         super(VSWorldData.class, VSWorldData::new);
 *     }
 * }
 * }</pre>
 *
 * @param <K> The type of object this capability should store
 * @see VSWorldDataCapability
 */
@Accessors(fluent = false)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Log4j2
public abstract class VSDefaultCapability<K> {

    @Getter(AccessLevel.PROTECTED)
    private final ObjectMapper mapper;
    private final Class<K> kClass;
    @Nonnull
    private K instance;
    private Supplier<K> factory;

    public VSDefaultCapability(Class<K> kClass, Supplier<K> factory) {
        this(kClass, factory, createMapper());
    }

    public VSDefaultCapability(Class<K> kClass, Supplier<K> factory, ObjectMapper mapper) {
        this.kClass = kClass;
        this.factory = factory;
        this.instance = factory.get();
        log.debug("CONSTRUCTED INSTANCE: " + instance);
        this.mapper = mapper;
    }

    private static ObjectMapper createMapper() {
        ObjectMapper mapper = new CBORMapper();

        mapper.setVisibility(mapper.getVisibilityChecker()
            .withFieldVisibility(Visibility.ANY)
            .withGetterVisibility(Visibility.NONE)
            .withIsGetterVisibility(Visibility.NONE)
            .withSetterVisibility(Visibility.NONE));

        mapper.addMixIn(AxisAlignedBB.class, AABBMixinSerializer.class);

        return mapper;
    }

    @Nullable
    public NBTTagByteArray writeNBT(EnumFacing side) {
        long time = System.currentTimeMillis();
        byte[] value;
        try {
            value = getMapper().writeValueAsBytes(instance);
            log.debug("VS serialization took {} ms. Writing data of size {} KB. ({})",
                System.currentTimeMillis() - time, value.length / Math.pow(2, 10),
                instance.getClass().getSimpleName());
        } catch (Exception ex) {
            log.fatal("Something just broke horrifically. Be wary of your data. "
                + "This will crash the game in future releases", ex);
            value = new byte[0];
        }
        return new NBTTagByteArray(value);
    }

    public K readNBT(NBTBase base, EnumFacing side) {
        long time = System.currentTimeMillis();

        byte[] value = ((NBTTagByteArray) base).getByteArray();
        try {
            this.instance = mapper.readValue(value, kClass);
        } catch (IOException ex) {
            log.fatal("Failed to read your ship data? Ships will probably be missing", ex);
            this.instance = factory.get();
        }

        // Possibly redundant null check. TODO: remove
        if (this.instance == null) {
            log.fatal("Failed to read your ship data? Ships will probably be missing");
            this.instance = factory.get();
        }

        log.info("VS deserialization took {} ms. Reading data of size {} KB.",
            System.currentTimeMillis() - time, value.length / Math.pow(2, 10));

        return this.instance;
    }

    public K get() {
        return instance;
    }

    public void set(K instance) {
        this.instance = instance;
    }

}
