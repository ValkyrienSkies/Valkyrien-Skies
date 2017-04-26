package code.elix_x.excomms.jstructure;

import java.util.function.Function;

public class JavaStructure {

	public static enum JModifier {

		//@formatter:off
		PUBLIC,
		PRIVATE,
		PROTECTED,
		STATIC,
		FINAL,
		SYNCHRONIZED,
		VOLATILE,
		TRANSIENT,
		NATIVE,
		INTERFACE,
		ABSTRACT,
		STRICT,
		BRIDGE,
		VARARGS,
		SYNTHETIC,
		ANNOTATION,
		ENUM,
		MANDATED;
		//@formatter:on

		private final int bit;
		private final int mask;

		private JModifier(){
			this.bit = ordinal();
			this.mask = (int) Math.pow(2, bit);
		}

		public int bit(){
			return bit;
		}

		public int mask(){
			return mask;
		}

	}

	public static enum JModifiersMask {

		//@formatter:off
		CLASS(3103),
		INTERFACE(3087),
		CONSTRUCTOR(7),
		METHOD(3391),
		FIELD(223),
		PARAMETER(16),
		ACCESS(7);
		//@formatter:on

		private final int mask;

		private JModifiersMask(int mask){
			this.mask = mask;
		}

	}

	public static final class JModifiers {

		private final int mask;

		public JModifiers(int mask){
			this.mask = mask;
		}

		public boolean is(JModifier modifier){
			return (mask & modifier.mask) != 0;
		}

		public JModifiers with(JModifier modifier, boolean active){
			return new JModifiers(active ? mask | modifier.mask : mask & (~modifier.mask));
		}

		public JModifiers mask(JModifiersMask mask){
			return new JModifiers(this.mask & mask.mask);
		}

	}

	public static interface JType<T> {

	}

	public static enum JPrimitive implements JType {

		//@formatter:off
		BOOLEAN(JClass.fromClass(boolean.class), JClass.fromClass(Boolean.class), 1),
		BYTE(JClass.fromClass(byte.class), JClass.fromClass(Byte.class), Byte.SIZE),
		SHORT(JClass.fromClass(short.class), JClass.fromClass(Short.class), Short.SIZE),
		INT(JClass.fromClass(int.class), JClass.fromClass(Integer.class), Integer.SIZE),
		LONG(JClass.fromClass(long.class), JClass.fromClass(Long.class), Long.SIZE),
		FLOAT(JClass.fromClass(float.class), JClass.fromClass(Float.class), Float.SIZE),
		DOUBLE(JClass.fromClass(double.class), JClass.fromClass(Double.class), Double.SIZE),
		CHAR(JClass.fromClass(char.class), JClass.fromClass(Character.class), Character.SIZE);
		//@formatter:on

		private final JClass primitive;
		private final JClass boxed;
		private final int bits;

		private JPrimitive(JClass primitive, JClass boxed, int bits){
			this.primitive = primitive;
			this.boxed = boxed;
			this.bits = bits;
		}

		public <T> JClass<T> primitive(){
			return primitive;
		}

		public <T> JClass<T> boxed(){
			return boxed;
		}

		public int bits(){
			return bits;
		}

		public int bytes(){
			return (int) Math.ceil(bits / 8f);
		}

	}

	public final class JParameter<T> {

		private final JModifiers modifiers;
		private final JType<T> type;

		public JParameter(JModifiers modifiers, JType<T> type){
			this.modifiers = modifiers.mask(JModifiersMask.PARAMETER);
			this.type = type;
		}

		public JModifiers modifiers(){
			return modifiers;
		}

		public JType<T> type(){
			return type;
		}

	}

	public static class JClass<T> implements JType<T> {

		// The One
		public static final JClass<Object> OBJECT = new JClass<Object>(new JModifiers(Object.class.getModifiers()), Object.class.getName(), null, new JInterface[0], clas -> new JClass.JField[0], clas -> new JClass.JConstructor[]{clas.new JConstructor(new JModifiers(JModifier.PUBLIC.mask()))}, clas -> new JClass.JMethod[]{});

		public static final <T> JClass<T> fromClass(Class<T> clas){
			return null;
		}

		private final JModifiers modifiers;
		private final String name;
		private final JClass<? super T> superClass;
		private final JInterface[] interfaces;
		private final JField[] fields;
		private final JConstructor[] constructors;
		private final JMethod[] methods;

		public JClass(JModifiers modifiers, String name, JClass<? super T> superClass, JInterface[] interfaces, Function<JClass<T>, JField[]> fields, Function<JClass<T>, JConstructor[]> constructors, Function<JClass<T>, JMethod[]> methods){
			this.modifiers = modifiers.mask(JModifiersMask.CLASS);
			this.name = name;
			this.superClass = superClass;
			this.interfaces = interfaces;
			this.fields = fields.apply(this);
			this.constructors = constructors.apply(this);
			this.methods = methods.apply(this);
		}

		public final JModifiers modifiers(){
			return modifiers;
		}

		public final String name(){
			return name;
		}

		public final JClass<? super T> superClass(){
			return superClass;
		}

		public JInterface[] interfaces(){
			return interfaces;
		}

		public final JField[] fields(){
			return fields;
		}

		public final JConstructor[] cConstructors(){
			return constructors;
		}

		public final JMethod[] methods(){
			return methods;
		}

		public final class JField<F> {

			private final JModifiers modifiers;
			private final JType<F> type;
			private final String name;

			public JField(JModifiers modifiers, JType<F> type, String name){
				this.modifiers = modifiers.mask(JModifiersMask.FIELD);
				this.type = type;
				this.name = name;
			}

			public JClass<T> clas(){
				return JClass.this;
			}

			public JModifiers modifiers(){
				return modifiers;
			}

			public JType<F> type(){
				return type;
			}

			public String name(){
				return name;
			}

		}

		public final class JConstructor {

			private final JModifiers modifiers;

			public JConstructor(JModifiers modifiers){
				this.modifiers = modifiers.mask(JModifiersMask.CONSTRUCTOR);
			}

			public JModifiers modifiers(){
				return modifiers;
			}

			public JClass<T> clas(){
				return JClass.this;
			}

		}

		public final class JMethod<R> {

			private final JModifiers modifiers;
			private final JType<R> type;
			private final String name;
			private final JParameter[] parameters;

			public JMethod(JModifiers modifiers, JType<R> type, String name, JParameter... parameters){
				this.modifiers = modifiers.mask(JModifiersMask.METHOD);
				this.type = type;
				this.name = name;
				this.parameters = parameters;
			}

			public JClass<T> clas(){
				return JClass.this;
			}

			public JModifiers modifiers(){
				return modifiers;
			}

			public JType<R> returnType(){
				return type;
			}

			public String name(){
				return name;
			}

			public JParameter[] parameters(){
				return parameters;
			}

		}

		/*@formatter:off
		public static final class JArray<T> extends JClass<T[]> {

			public JArray(JModifiers modifiers){
				super(modifiers, name, superClass, fields, constructors, methods);
			}

		}
		@formatter:on*/

		public static final class JInterface<T> extends JClass<T> {

			public JInterface(JModifiers modifiers, String name, JInterface[] interfaces, Function<JClass<T>, JMethod[]> methods){
				super(modifiers.with(JModifier.ABSTRACT, true).with(JModifier.INTERFACE, true).mask(JModifiersMask.INTERFACE), name, null, interfaces, clas -> new JClass.JField[0], clas -> new JClass.JConstructor[0], methods);
			}

		}

	}

}
