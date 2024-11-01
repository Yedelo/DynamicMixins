package at.yedel.dynamicmixins;



import java.util.Arrays;
import java.util.List;



public class DynamicMixin {
	private final String mixinDomain;
	private final String name;
	private final String targettedClassName;
	private final List<DynamicGenerator> dynamicGenerators;
	private final boolean verbose;

	public DynamicMixin(String mixinDomain, String name, String targettedClassName, List<DynamicGenerator> dynamicGenerators, boolean verbose) {
		this.mixinDomain = mixinDomain;
		this.name = name;
		this.targettedClassName = targettedClassName;
		this.dynamicGenerators = dynamicGenerators;
		this.verbose = verbose;
	}

	public DynamicMixin(String mixinDomain, String name, String targettedClassName, List<DynamicGenerator> dynamicGenerators) {
		this(mixinDomain, name, targettedClassName, dynamicGenerators, true);
	}

	public DynamicMixin(String mixinDomain, String name, String targettedClassName, DynamicGenerator[] dynamicGenerators, boolean verbose) {
		this(mixinDomain, name, targettedClassName, Arrays.asList(dynamicGenerators), verbose);
	}

	public DynamicMixin(String mixinDomain, String name, String targettedClassName, DynamicGenerator[] dynamicGenerators) {
		this(mixinDomain, name, targettedClassName, Arrays.asList(dynamicGenerators), true);
	}

	/**
	 * Returns the domain of this mixin, which generally represents the "parent" of this mixin and is used as the package name.
	 *
	 * @return the mixin domain
	 */
	public String getMixinDomain() {
		return mixinDomain;
	}

	/**
	 * Returns the name of the dynamic mixin, used for the class name.
	 * Example: "DynamicMixinMinecraft"
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the name of the targetted class, using dots.
	 * Example: "net.minecraft.client.Minecraft"
	 *
	 * @return the targetted class name
	 */
	public String getTargettedClassName() {
		return targettedClassName;
	}

	/**
	 * Returns a list of dynamic generators used to generate mixin methods.
	 *
	 * @return the dynamic generators
	 */
	public List<DynamicGenerator> getDynamicGenerators() {
		return dynamicGenerators;
	}

	/**
	 * Returns if this dynamic mixin is verbose. If it is, Mixin will log a message when this mixin is ran.
	 * @return if this dynamic mixin is verbose
	 */
	public boolean isVerbose() {
		return verbose;
	}
}
