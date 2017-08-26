package ValkyrienWarfareBase.API;

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;

import javax.annotation.Nullable;

public class StupidPredicate implements Predicate<Entity> {

	public boolean apply(@Nullable Entity p_apply_1_) {
		return p_apply_1_ != null && p_apply_1_.canBeCollidedWith();
	}

}
