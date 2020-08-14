package org.valkyrienskies.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Todo: The ladder code should be deleted and everything else should be replaced with capabilities
 * and events.
 */
@Deprecated
@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity {

    private final EntityLivingBase thisAsEntity = EntityLivingBase.class.cast(this);

    /**
     * This constructor is needed to make java compile this class, but doesn't actually affect
     * anything
     */
    public MixinEntityLivingBase(World world) {
        super(world);
    }


}
