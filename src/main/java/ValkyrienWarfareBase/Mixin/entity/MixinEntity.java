package ValkyrienWarfareBase.Mixin.entity;

import ValkyrienWarfareBase.Interaction.EntityDraggable;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
//TODO: make this work
//this doesn't actually change the superclass, instead it crashes the game
public abstract class MixinEntity extends EntityDraggable {
}
