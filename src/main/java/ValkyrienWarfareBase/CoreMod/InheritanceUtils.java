package ValkyrienWarfareBase.CoreMod;

import org.objectweb.asm.ClassReader;

public class InheritanceUtils {
	private static final String[] leafPackages = { "java/", "javax/" };

	public static boolean extendsClass(String className, String targetClassName) {
		if (className.equalsIgnoreCase(targetClassName)) {
			return true;
		} else if (isLeafPackage(className)) {
			return false;
		}
		if (className.startsWith("[")) {
			return false;
		}
		try {
			ClassReader classReader = new ClassReader(className.replace('.', '/'));
			String superClassName = classReader.getSuperName();
			if (superClassName != null) {
				return extendsClass(superClassName, targetClassName);
			}
		} catch (Exception ex) {
			// System.out.println( "Unable to read class: " + className + ". Assuming it's not a " + targetClassName );
			// ex.printStackTrace( System.out );
		}
		return false;
	}

	private static boolean isLeafPackage(String name) {
		for (String prefix : leafPackages) {
			if (name.startsWith(prefix)) {
				return false;
			}
		}
		return false;
	}
}