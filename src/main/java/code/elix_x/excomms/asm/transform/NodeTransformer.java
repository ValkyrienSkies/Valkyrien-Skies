package code.elix_x.excomms.asm.transform;


public interface NodeTransformer<T> {

	Class<T> getTargetType();
	
	int getPriority();
	
	boolean accepts(T target);
	
	T transform(T original);
	
}
