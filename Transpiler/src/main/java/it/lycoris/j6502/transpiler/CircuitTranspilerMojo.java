package it.lycoris.j6502.transpiler;

import it.lycoris.j6502.transpiler.data.ChipDefinition;
import it.lycoris.j6502.transpiler.data.ComponentDefinition;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@Mojo(name = "generate-circuits", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class CircuitTranspilerMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project.basedir}/src/main/resources/hardware", property = "sourceDirectory", required = true)
    private File sourceDirectory;
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/circuits", property = "outputDirectory", required = true)
    private File outputDirectory;
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CircuitCodeGenerator codeGenerator = new CircuitCodeGenerator();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!sourceDirectory.exists() || !sourceDirectory.isDirectory()) {
            getLog().warn("Source directory does " + sourceDirectory.getAbsolutePath() + " does not exist. Skipping transpilation.");
            return;
        }

        if (!outputDirectory.exists()) {
            boolean dirsCreated = outputDirectory.mkdirs();
            if (!dirsCreated) throw new MojoExecutionException("Could not create output directory: " + outputDirectory.getAbsolutePath());
        }

        try (Stream<Path> paths = Files.walk(Paths.get(sourceDirectory.getAbsolutePath()))) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".json"))
                    .forEach(this::transpileFile);
        } catch (Exception e) {
            throw new MojoExecutionException("Error traversing source directory", e);
        }

        project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
        getLog().info("Added " + outputDirectory.getAbsolutePath() + " to compile source roots.");
    }

    private void transpileFile(Path jsonFilePath) {
        getLog().info("Transiping circuit: " + jsonFilePath.getFileName());

        try {
            ChipDefinition chipDefinition = objectMapper.readValue(jsonFilePath.toFile(), ChipDefinition.class);

            CircuitGraphBuilder graphBuilder = new CircuitGraphBuilder(chipDefinition);
            List<ComponentDefinition> topologicalOrder = graphBuilder.getTopologicalSort();
            codeGenerator.generateChipClass(chipDefinition, topologicalOrder, outputDirectory);

            getLog().info("Successfully generated class for: " + chipDefinition.chipName());
        } catch (Exception exception) {
            getLog().error("Failed to transpile file: " + jsonFilePath.getFileName(), exception);
            throw new RuntimeException("Transpilation failed for " + jsonFilePath.getFileName(), exception);
        }
    }
}
