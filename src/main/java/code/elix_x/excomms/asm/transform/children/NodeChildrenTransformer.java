package code.elix_x.excomms.asm.transform.children;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

import code.elix_x.excomms.asm.transform.NodeTransformer;

public abstract class NodeChildrenTransformer<T> implements NodeTransformer<T> {

	private final ImmutableList<Pair<Function<T, Pair<Class, Collection<?>>>, NodeTransformer>> nodes;
	private final Function<Triple<T, ?, ?>, T> modified;

	/**
	 * Creates new base asm transformer. Each function of multimap is used to retrieve a collection of child nodes from this node, which are transformed through node transformers and are passed to the modification function which returns new work node for this transformer (or old one if modifications do not require re-instantiation).<br>
	 * <br>
	 * 
	 * <ul>
	 * <li>If parent2children function is <tt>null</tt>, node transformers are considered as accepting same node type as this transformer.</li>
	 * <li>If collection of child nodes contains <tt>null</tt>, addition of new nodes is allowed.</li>
	 * <li>If old2modified function accepts <tt>null</tt> as new child node, deletion of child nodes is allowed.</li>
	 * </ul>
	 * 
	 * @param nodes
	 *            multimap of functions to transformer nodes. Each function is used to retrieve a collection of nodes from this node, which are transformed through node transformers.<br>
	 *            If function is <tt>null</tt>, node transformers are considered as accepting same node type as this transformer.<br>
	 *            If collection of child nodes contains <tt>null</tt>, addition of new nodes is allowed.
	 * @param modified
	 *            function accepting triple of parent node, old child node and new child node, and returning new parent node (or old one if modifications do not require re-instantiation).<br>
	 *            If function accepts <tt>null</tt> as new child node, deletion of child nodes is allowed.
	 */
	public NodeChildrenTransformer(Multimap<Function<T, Pair<Class, Collection<?>>>, NodeTransformer> nodes, Function<Triple<T, ?, ?>, T> modified){
		Set<Pair<Function<T, Pair<Class, Collection<?>>>, NodeTransformer>> dispatched = new TreeSet<Pair<Function<T, Pair<Class, Collection<?>>>, NodeTransformer>>((pair1, pair2) -> pair1.getRight().getPriority() - pair2.getRight().getPriority());
		dispatched.addAll(Collections2.transform(nodes.entries(), entry -> new ImmutablePair<>(entry.getKey(), entry.getValue())));
		this.nodes = ImmutableList.copyOf(dispatched);
		this.modified = modified;
	}

	@Override
	public T transform(T parent){
		for(Pair<Function<T, Pair<Class, Collection<?>>>, NodeTransformer> pair : nodes){
			NodeTransformer node = pair.getRight();
			if(pair.getLeft() != null){
				Pair<Class, Collection<?>> res = pair.getLeft().apply(parent);
				for(Object target : res.getRight())
					if(node.getTargetType().isAssignableFrom(res.getLeft()) && node.accepts(target))
						parent = modified.apply(new ImmutableTriple<>(parent, target, node.transform(target)));
			} else{
				if(node.getTargetType().isAssignableFrom(parent.getClass()) && node.accepts(parent))
					parent = modified.apply(new ImmutableTriple<>(parent, parent, node.transform(parent)));
			}
		}
		return parent;
	}

	public static class Builder<T> {

		private Multimap<Function<T, Pair<Class, Collection<?>>>, NodeTransformer> nodes = HashMultimap.create();
		private Map<Class, Function<Triple<T, ?, ?>, T>> modified = new HashMap<>();

		/**
		 * Appends *any* transformer node.
		 * 
		 * @param parent2children
		 *            function returning children nodes
		 * @param transformer
		 *            children node transformer. Children type safety is guaranteed.
		 * @param modified
		 *            function applying transformer modifications<br>
		 * @return <tt>this</tt>
		 */
		public Builder node(Function<T, Pair<Class, Collection<?>>> parent2children, NodeTransformer transformer, Function<Triple<T, ?, ?>, T> modified){
			nodes.put(parent2children, transformer);
			this.modified.put(transformer.getTargetType(), modified);
			return this;
		}

		protected Builder nodeG(Function parent2children, NodeTransformer transformer, Function modified){
			return node(parent2children, transformer, modified);
		}

		protected Multimap<Function<T, Pair<Class, Collection<?>>>, NodeTransformer> nodes(){
			return nodes;
		}

		protected Function<Triple<T, ?, ?>, T> modified(){
			return modified.entrySet().stream().collect(Collectors.reducing(triple -> triple.getLeft(), entry -> triple -> (triple.getMiddle() != null && entry.getKey().isAssignableFrom(triple.getMiddle().getClass())) || (triple.getRight() != null && entry.getKey().isAssignableFrom(triple.getRight().getClass())) ? entry.getValue().apply(triple) : triple.getLeft(), (f1, f2) -> triple -> f2.apply(new ImmutableTriple<>(f1.apply(triple), triple.getMiddle(), triple.getRight()))));
		}

	}

}
