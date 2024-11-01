package at.yedel.dynamicmixins;



import org.objectweb.asm.tree.ClassNode;



public abstract class DynamicGenerator {
	/**
	 * Modifies the mixin class node by generating methods.
	 *
	 * @param classNode The mixin class node
	 */
	public abstract void generate(ClassNode classNode);
}
