package ValkyrienWarfareBase.API;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.entity.Entity;

public class StupidPredicate implements Predicate<Entity >{

	public boolean apply(@Nullable Entity p_apply_1_) {
		return p_apply_1_ != null && p_apply_1_.canBeCollidedWith();
	}

}
