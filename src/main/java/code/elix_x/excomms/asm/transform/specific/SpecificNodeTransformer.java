package code.elix_x.excomms.asm.transform.specific;

import java.util.function.Consumer;
import java.util.function.Function;

import code.elix_x.excomms.asm.transform.NodeTransformer;

public abstract class SpecificNodeTransformer<T> implements NodeTransformer<T> {

	private final Class<T> target;
	private final int priority;
	private final Function<T, T> transform;

	public SpecificNodeTransformer(Class<T> target, int priority, Function<T, T> transform){
		this.target = target;
		this.priority = priority;
		this.transform = transform;
	}

	public SpecificNodeTransformer(int priority, Class<T> target, Consumer<T> transform){
		this(target, priority, old -> {
			transform.accept(old);
			return old;
		});
	}

	@Override
	public Class<T> getTargetType(){
		return target;
	}

	@Override
	public int getPriority(){
		return priority;
	}

	@Override
	public T transform(T original){
		return transform.apply(original);
	}

}
