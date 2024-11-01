package at.yedel.dynamicmixins;



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.Mixins;



public class DynamicMixins implements Opcodes {
	public static final String version = "#version#";
	private static final Logger loggi = LogManager.getLogger("DynamicMixins");
	private static final File dynamicMixinsFolder = new File("DynamicMixins");
	private static final LaunchClassLoader launchClassLoader = Launch.classLoader;
	private static final Method $addURL;

	static {
		loggi.info("Starting DynamicMixins {}", version);
		loggi.info("Current class loader: {}", DynamicMixins.class.getClassLoader());
		try {
			$addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
		}
		catch (NoSuchMethodException e) {
			loggi.error("Couldn't find addURL method in URLClassLoader!??!?!");
			throw new RuntimeException(e);
		}
		$addURL.setAccessible(true);
		if (dynamicMixinsFolder.mkdir()) {
			for (File file: dynamicMixinsFolder.listFiles()) {
				file.delete();
			}
		}
		else if (!dynamicMixinsFolder.exists()) {
			loggi.error("Couldn't make DynamicMixins folder!");
			throw new RuntimeException("Couldn't make DynamicMixins folder!");
		}
		try {
			addURLToClasspath(dynamicMixinsFolder.toURI().toURL());
		}
		catch (InvocationTargetException | IllegalAccessException | MalformedURLException e) {
			loggi.error("Couldn't add dynamic mixins folder URL to classpath!");
			throw new RuntimeException(e);
		}
	}

	/**
	 * Generates and registers a dynamic mixin class.
	 *
	 * @param dynamicMixin
	 */
	public static void addDynamicMixin(DynamicMixin dynamicMixin) {
		String dynamicMixinName = dynamicMixin.getName();
		String className = "at.yedel.dynamicmixins.generated." + dynamicMixin.getMixinDomain().toLowerCase() + "." + dynamicMixinName;

		ClassNode classNode = new ClassNode();
		classNode.access = ACC_PUBLIC | ACC_ABSTRACT;
		classNode.methods = new ArrayList<MethodNode>();
		classNode.superName = "java/lang/Object";
		classNode.name = className.replace(".", "/");
		// Example: "TestModule > DynamicMixinMinecraft (DynamicMixins)"
		// Does this actually do anything though? Not sure
		classNode.sourceFile = dynamicMixin.getMixinDomain() + " > " + dynamicMixinName + " (DynamicMixins)";
		classNode.version = V1_8;

		AnnotationNode mixinAnnotationNode = new AnnotationNode("Lorg/spongepowered/asm/mixin/Mixin;");
		mixinAnnotationNode.values = new ArrayList<>();
		mixinAnnotationNode.values.add("value");
		mixinAnnotationNode.values.add(Collections.singletonList(dynamicMixin.getTargettedClassName().replace(".", "/")));
		classNode.invisibleAnnotations = Collections.singletonList(mixinAnnotationNode);

		for (DynamicGenerator dynamicGenerator: dynamicMixin.getDynamicGenerators()) {
			dynamicGenerator.generate(classNode);
		}

		ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		classNode.accept(classWriter);
		byte[] classBytes = classWriter.toByteArray();

		int lastDotIndex = className.lastIndexOf(".");
		String packageName = className.replace(".", "/").substring(0, lastDotIndex);
		String simpleClassName = className.substring(lastDotIndex + 1);
		File generatedPackageFolder = new File(dynamicMixinsFolder, packageName);
		generatedPackageFolder.mkdirs();
		File mixinClassFile = new File(generatedPackageFolder, simpleClassName + ".class");
		try (FileOutputStream fileOutputStream = new FileOutputStream(mixinClassFile)) {
			fileOutputStream.write(classBytes);
		}
		catch (IOException e) {
			loggi.error("Couldn't save dynamic mixin class {}!", dynamicMixinName);
			throw new RuntimeException(e);
		}

		// Example: "dynamicmixins.testmodule-dynamicmixinminecraft.json"
		String mixinConfigName = "dynamicmixins." + dynamicMixin.getMixinDomain().toLowerCase() + "-" + dynamicMixinName.toLowerCase() + ".json";
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("compatibilityLevel", new JsonPrimitive("JAVA_8"));
		jsonObject.add("setSourceFile", new JsonPrimitive(true));
		jsonObject.add("minVersion", new JsonPrimitive("0.8"));
		JsonArray mixinArray = new JsonArray();
		mixinArray.add(new JsonPrimitive(dynamicMixinName));
		jsonObject.add("mixins", mixinArray);
		jsonObject.add("package", new JsonPrimitive("at.yedel.dynamicmixins.generated." + dynamicMixin.getMixinDomain().toLowerCase()));
		jsonObject.add("verbose", new JsonPrimitive(dynamicMixin.isVerbose()));
		byte[] mixinConfigBytes = jsonObject.toString().getBytes();

		File mixinConfigFile = new File(dynamicMixinsFolder, mixinConfigName);
		try (FileOutputStream outputStream = new FileOutputStream(mixinConfigFile)) {
			outputStream.write(mixinConfigBytes);
		}
		catch (IOException e) {
			loggi.error("Couldn't save dynamic mixin config {}!", mixinConfigName);
			throw new RuntimeException(e);
		}

		Mixins.addConfiguration(mixinConfigName);
	}

	/**
	 * Adds a URL to the launch class loader.
	 *
	 * @param url
	 * @throws InvocationTargetException unexpected
	 * @throws IllegalAccessException unexpected
	 */
	private static void addURLToClasspath(URL url) throws InvocationTargetException, IllegalAccessException {
		launchClassLoader.addURL(url);
		$addURL.invoke(launchClassLoader.getClass().getClassLoader(), url);
	}
}
