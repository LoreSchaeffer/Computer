package it.lycoris.lycoscript.compiler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ProjectScaffolder {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectScaffolder.class);
    private static final ClassLoader CLASS_LOADER = ProjectScaffolder.class.getClassLoader();
    private static final String[] stdlib = new String[]{"audio", "graphics", "math", "stdio"};

    public static void createNewProject(String projectName) {
        LOG.info("Scaffolding new LycoScript project: {}", projectName);

        File projectDir = new File(projectName);
        if (projectDir.exists()) {
            LOG.error("Directory '{}' already exists. Aborting.", projectName);
            System.exit(1);
        }

        try {
            boolean created = projectDir.mkdirs();
            if (!created) throw new IOException("Could not create project root directory.");

            File srcDir = new File(projectDir, "src");
            File stdlibDir = new File(projectDir, "stdlib");
            File stdlibHeaders = new File(stdlibDir, "headers");
            File stdlibAssembly = new File(stdlibDir, "asm");
            File buildDir = new File(projectDir, "build");

            srcDir.mkdirs();
            stdlibHeaders.mkdirs();
            stdlibAssembly.mkdirs();
            buildDir.mkdirs();

            extractFile("data/main.ls", Path.of(srcDir.getPath(), "main.ls"));
            extractFile("data/build.bat", Path.of(projectDir.getPath(), "build.bat"));
            extractFile("data/build.sh", Path.of(projectDir.getPath(), "build.sh"));
            extractFile("data/lyco8.cfg", Path.of(projectDir.getPath(), "lyco8.cfg"));

            for (String lib : stdlib) {
                extractFile("stdlib/headers/" + lib + ".lh", Path.of(stdlibHeaders.getPath(), lib + ".lh"));
                extractFile("stdlib/asm/" + lib + ".asm", Path.of(stdlibAssembly.getPath(), lib + ".asm"));
            }

            copyRunningJar(projectDir);

            LOG.info("Project '{}' initialized successfully!", projectName);
            LOG.info("Navigate to the folder and run 'build.bat' or './build.sh' to compile.");
        } catch (IOException e) {
            LOG.error("Failed to scaffold project", e);
            System.exit(1);
        }
    }

    private static void extractFile(String path, Path dst) throws IOException {
        try (InputStream is = CLASS_LOADER.getResourceAsStream(path)) {
            if (is == null) throw new IOException("Could not load resource: " + path);
            Files.copy(is, dst, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void copyRunningJar(File projectDir) {
        try {
            URI jarUri = ProjectScaffolder.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            File currentJar = new File(jarUri);

            if (currentJar.isFile() && currentJar.getName().endsWith(".jar")) {
                Path destinationPath = projectDir.toPath().resolve("LycoScriptCompiler.jar");
                Files.copy(currentJar.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                LOG.info("Successfully deployed running compiler JAR to target workspace: LycoScriptCompiler.jar");
            } else {
                LOG.warn("Compiler is not executing from a packaged JAR file (likely IDE execution). Skipping self-copy.");
            }
        } catch (Exception e) {
            LOG.error("Failed to copy running JAR into the project directory structure", e);
        }
    }
}
