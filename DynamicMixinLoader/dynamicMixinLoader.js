export default ASM => {
    loadDynamicMixins(ASM.currentModule);
}

function loadDynamicMixins(currentModule) {
    const LogManager = Java.type("org.apache.logging.log4j.LogManager");
    const URLClassLoader = Java.type("java.net.URLClassLoader");
    const URL = Java.type("java.net.URL");
    const Launch = Java.type("net.minecraft.launchwrapper.Launch");
    const FilenameFilter = Java.type("java.io.FilenameFilter");
    const File = Java.type("java.io.File");
    const Class = Java.type("java.lang.Class");

    const logger = LogManager.getLogger("DynamicMixinLoader");
    logger.info("Loading DynamicMixins with ChatTriggers from module {}", currentModule);

    const $addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
    $addURL.setAccessible(true);
    const launchClassLoader = Launch.classLoader;
    const filenameFilter = new JavaAdapter(FilenameFilter, {
        accept​(dir, name) {
            return name.endsWith(".jar");
        }
    });
    const jarFiles = new File(`config/ChatTriggers/modules/${currentModule}`).listFiles(filenameFilter);
    if (jarFiles.length == 0) {
        logger.error("No jar files in module folder! Not loading dynamic mixins...");
        return;
    }
    jarFiles.forEach(file => {
        logger.info("Adding .jar file {} to classpath", file.getName());
        $addURL.invoke(
            launchClassLoader, 
            file.toURI().toURL()
        );
    });
    
    Class.forName("at.yedel.dynamicmixins.DynamicMixins", true, launchClassLoader);
}