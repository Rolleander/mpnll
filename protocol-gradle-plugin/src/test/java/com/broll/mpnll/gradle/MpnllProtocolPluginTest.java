package com.broll.mpnll.gradle;

import static org.junit.Assert.assertNotNull;

import org.gradle.testkit.runner.GradleRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MpnllProtocolPluginTest {

    @Rule
    public final TemporaryFolder projectDirectory = new TemporaryFolder();

    @Test
    public void generatesAndPackagesSharedProtocolSources() throws Exception {
        write("settings.gradle", "rootProject.name = 'game-protocol'\n");
        write(
            "build.gradle",
            "plugins { id 'com.broll.mpnll.protocol' }\n"
                + "repositories { mavenCentral() }\n"
                + "sourceSets.main.java.srcDirs = ['src/main/java']\n"
        );
        write(
            "src/main/proto/example/protocol/game.proto",
            "syntax = \"proto3\";\n"
                + "option java_multiple_files = true;\n"
                + "option java_package = \"example.protocol\";\n"
                + "message Ping { string value = 1; }\n"
        );
        write(
            "src/main/java/example/protocol/GameProtocol.gwt.xml",
            "<module><source path=\"\" /></module>\n"
        );

        GradleRunner.create()
            .withProjectDir(projectDirectory.getRoot())
            .withPluginClasspath()
            .withArguments("jar", "--stacktrace")
            .build();

        File jarFile = new File(projectDirectory.getRoot(), "build/libs/game-protocol.jar");
        try (ZipFile jar = new ZipFile(jarFile)) {
            assertEntry(jar, "example/protocol/Ping.class");
            assertEntry(jar, "example/protocol/Ping.java");
            assertEntry(jar, "example/protocol/game.proto");
            assertEntry(jar, "example/protocol/GameProtocol.gwt.xml");
        }
    }

    private void write(String path, String contents) throws IOException {
        File file = new File(projectDirectory.getRoot(), path);
        Files.createDirectories(file.toPath().getParent());
        try (FileOutputStream output = new FileOutputStream(file)) {
            output.write(contents.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static void assertEntry(ZipFile jar, String path) {
        ZipEntry entry = jar.getEntry(path);
        assertNotNull("Missing JAR entry " + path, entry);
    }
}
