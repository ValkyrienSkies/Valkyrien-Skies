package org.valkyrienskies.mod.common.capability.framework;

import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mcp.MethodsReturnNonnullByDefault;

/**
 * Like {@link VSDefaultCapability} but does not serialize anything, and has no guarantee that the
 * instance is present
 *
 * @param <K>
 */
@MethodsReturnNonnullByDefault
public class VSDefaultCapabilityTransientOptional<K> {

    private K instance;

    public VSDefaultCapabilityTransientOptional() {
        this((K) null);
    }

    public VSDefaultCapabilityTransientOptional(K inst) {
        this.instance = inst;
    }

    public VSDefaultCapabilityTransientOptional(@Nonnull Supplier<K> factory) {
        this.instance = factory.get();
    }

    public Optional<K> get() {
        return Optional.ofNullable(instance);
    }

    public void set(K instance) {
        this.instance = instance;
    }

}
