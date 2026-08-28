package com.broll.mpnll.gradle;

import com.google.protobuf.gradle.ProtobufExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class MpnllProtocolPlugin implements Plugin<Project> {

    private static final String PROTOBUF_VERSION = loadProtobufVersion();

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("java-library");
        project.getPluginManager().apply("com.google.protobuf");

        project.getDependencies().add(
            JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME,
            "com.google.protobuf:protobuf-java:" + PROTOBUF_VERSION
        );

        project.getExtensions().configure(
            ProtobufExtension.class,
            protobuf -> protobuf.protoc(
                protoc -> protoc.setArtifact("com.google.protobuf:protoc:" + PROTOBUF_VERSION)
            )
        );

        SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        SourceSet main = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        Provider<Directory> generatedJava = project.getLayout().getBuildDirectory()
            .dir("generated/source/proto/main/java");
        project.getTasks().named(
            JavaPlugin.COMPILE_JAVA_TASK_NAME,
            JavaCompile.class
        ).configure(compileJava -> {
            compileJava.dependsOn("generateProto");
            compileJava.source(generatedJava);
        });
        project.getTasks().named(JavaPlugin.JAR_TASK_NAME, Jar.class).configure(jar -> {
            jar.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);
            jar.from(main.getAllJava());
            jar.from(generatedJava);
            jar.from(project.file("src/main/proto"), spec -> spec.include("**/*.proto"));
            jar.from(project.file("src/main/java"), spec -> spec.include("**/*.gwt.xml"));
        });
    }

    private static String loadProtobufVersion() {
        Properties properties = new Properties();
        try (InputStream input = MpnllProtocolPlugin.class.getClassLoader()
            .getResourceAsStream("mpnll-protocol-plugin.properties")) {
            if (input == null) {
                throw new IllegalStateException("Missing MPNLL protocol plugin properties");
            }
            properties.load(input);
            return properties.getProperty("protobufVersion");
        } catch (IOException error) {
            throw new IllegalStateException("Could not load MPNLL protocol plugin properties", error);
        }
    }
}
