/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.pbt;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeContainer;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for the JEE Migration v4 spec.
 * These tests scan actual project files to verify migration correctness.
 */
class MigrationPropertiesTest {

    private static Path projectRoot;

    /**
     * Banned javax.* import patterns for Jakarta EE APIs.
     * javax.script.* is excluded (JDK API, not Jakarta EE).
     * javax.cache.* is excluded (JCache API, stays javax).
     */
    private static final List<Pattern> BANNED_JAVAX_PATTERNS = List.of(
            Pattern.compile("^import\\s+javax\\.enterprise\\."),
            Pattern.compile("^import\\s+javax\\.inject\\."),
            Pattern.compile("^import\\s+javax\\.ws\\.rs\\."),
            Pattern.compile("^import\\s+javax\\.validation\\."),
            Pattern.compile("^import\\s+javax\\.persistence\\."),
            Pattern.compile("^import\\s+javax\\.ejb\\."),
            Pattern.compile("^import\\s+javax\\.servlet\\."),
            Pattern.compile("^import\\s+javax\\.annotation\\."),
            Pattern.compile("^import\\s+javax\\.json\\."),
            Pattern.compile("^import\\s+org\\.apache\\.deltaspike\\.")
    );

    /**
     * Banned JUnit 4 import patterns.
     */
    private static final List<Pattern> BANNED_JUNIT4_PATTERNS = List.of(
            Pattern.compile("^import\\s+org\\.junit\\.Test\\b"),
            Pattern.compile("^import\\s+org\\.junit\\.Before\\b"),
            Pattern.compile("^import\\s+org\\.junit\\.After\\b"),
            Pattern.compile("^import\\s+org\\.junit\\.Assert\\b"),
            Pattern.compile("^import\\s+org\\.junit\\.runner\\.RunWith\\b")
    );

    private static final String EXPECTED_VERSION = "4.1.0-SNAPSHOT";

    private static final List<String> MODULE_DIRS = List.of(
            "demoiselle-core",
            "demoiselle-configuration",
            "demoiselle-crud",
            "demoiselle-rest",
            "demoiselle-script",
            "demoiselle-security",
            "demoiselle-security-jwt",
            "demoiselle-security-token",
            "demoiselle-security-hashcash",
            "demoiselle-parent-bom",
            "demoiselle-parent",
            "demoiselle-parent-rest"
    );

    @BeforeContainer
    static void resolveProjectRoot() {
        // Walk up from the test class output directory to find the project root (contains pom.xml)
        Path candidate = Paths.get("").toAbsolutePath();
        // If running from a module directory, go up one level
        if (Files.exists(candidate.resolve("pom.xml"))) {
            // Check if this is the root or a module
            if (Files.exists(candidate.resolve("demoiselle-core"))) {
                projectRoot = candidate;
            } else {
                projectRoot = candidate.getParent();
            }
        } else {
            projectRoot = candidate;
        }
    }

    // ========================================================================
    // P1: Ausência de imports javax.* Jakarta EE em todos os arquivos fonte
    // ========================================================================

    @Provide
    Arbitrary<Path> sourceFiles() {
        List<Path> files = collectSourceFiles();
        if (files.isEmpty()) {
            return Arbitraries.just(Paths.get("NO_SOURCE_FILES_FOUND"));
        }
        return Arbitraries.of(files);
    }

    /**
     * Feature: jee-migration-v4, Property 1: Ausência de imports javax.* Jakarta EE
     *
     * Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 1.10, 17.1, 17.2
     *
     * For any source file in the framework (except demoiselle-script for javax.script.*),
     * no import should use banned javax.* or org.apache.deltaspike.* namespaces.
     */
    @Property(tries = 200)
    @Tag("Feature_jee-migration-v4_Property-1_no-javax-jakarta-ee-imports")
    void noJavaxJakartaEEImportsInSourceFiles(@ForAll("sourceFiles") Path sourceFile) throws IOException {
        List<String> lines = Files.readAllLines(sourceFile);
        boolean isScriptModule = sourceFile.toString().contains("demoiselle-script");

        List<String> violations = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (!line.startsWith("import")) continue;

            // Skip javax.script.* in demoiselle-script module (JDK API)
            if (isScriptModule && line.matches("^import\\s+javax\\.script\\..*")) continue;

            for (Pattern banned : BANNED_JAVAX_PATTERNS) {
                if (banned.matcher(line).find()) {
                    violations.add("Line " + (i + 1) + ": " + line);
                }
            }
        }

        assertTrue(violations.isEmpty(),
                "File " + projectRoot.relativize(sourceFile) + " contains banned javax/deltaspike imports:\n"
                        + String.join("\n", violations));
    }

    // ========================================================================
    // P2: Ausência de imports JUnit 4 em todos os arquivos de teste
    // ========================================================================

    @Provide
    Arbitrary<Path> testFiles() {
        List<Path> files = collectTestFiles();
        if (files.isEmpty()) {
            return Arbitraries.just(Paths.get("NO_TEST_FILES_FOUND"));
        }
        return Arbitraries.of(files);
    }

    /**
     * Feature: jee-migration-v4, Property 2: Ausência de imports JUnit 4
     *
     * Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5, 6.6
     *
     * For any test file, no import should reference JUnit 4 annotations or classes.
     */
    @Property(tries = 200)
    @Tag("Feature_jee-migration-v4_Property-2_no-junit4-imports")
    void noJUnit4ImportsInTestFiles(@ForAll("testFiles") Path testFile) throws IOException {
        List<String> lines = Files.readAllLines(testFile);

        List<String> violations = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (!line.startsWith("import")) continue;

            for (Pattern banned : BANNED_JUNIT4_PATTERNS) {
                if (banned.matcher(line).find()) {
                    violations.add("Line " + (i + 1) + ": " + line);
                }
            }
        }

        assertTrue(violations.isEmpty(),
                "File " + projectRoot.relativize(testFile) + " contains JUnit 4 imports:\n"
                        + String.join("\n", violations));
    }

    // ========================================================================
    // P5: Versão 4.0.0-SNAPSHOT em todos os POMs de módulo
    // ========================================================================

    @Provide
    Arbitrary<Path> modulePoms() {
        List<Path> poms = collectModulePoms();
        if (poms.isEmpty()) {
            return Arbitraries.just(Paths.get("NO_POMS_FOUND"));
        }
        return Arbitraries.of(poms);
    }

    /**
     * Feature: jee-migration-v4, Property 5: Versão 4.0.0-SNAPSHOT em todos os POMs
     *
     * Validates: Requirements 16.1, 3.1
     *
     * For any module POM, the version tag should be 4.0.0-SNAPSHOT.
     */
    @Property(tries = 50)
    @Tag("Feature_jee-migration-v4_Property-5_version-4.0.0-SNAPSHOT-all-poms")
    void allModulePomsShouldHaveCorrectVersion(@ForAll("modulePoms") Path pomFile) throws IOException {
        String content = Files.readString(pomFile);

        // Extract the project-level <version> (not inside <parent> or <dependency>)
        // We look for <version> that appears after <artifactId> and before <parent> or after </parent>
        String version = extractProjectVersion(content);

        assertNotNull(version,
                "Could not extract project version from " + projectRoot.relativize(pomFile));
        assertEquals(EXPECTED_VERSION, version,
                "POM " + projectRoot.relativize(pomFile) + " has version '" + version
                        + "' instead of '" + EXPECTED_VERSION + "'");
    }

    // ========================================================================
    // Helper methods
    // ========================================================================

    private List<Path> collectSourceFiles() {
        List<Path> result = new ArrayList<>();
        for (String moduleDir : MODULE_DIRS) {
            Path srcMain = projectRoot.resolve(moduleDir).resolve("src/main/java");
            if (Files.isDirectory(srcMain)) {
                result.addAll(findJavaFiles(srcMain));
            }
        }
        return result;
    }

    private List<Path> collectTestFiles() {
        List<Path> result = new ArrayList<>();
        for (String moduleDir : MODULE_DIRS) {
            Path srcTest = projectRoot.resolve(moduleDir).resolve("src/test/java");
            if (Files.isDirectory(srcTest)) {
                result.addAll(findJavaFiles(srcTest));
            }
        }
        return result;
    }

    private List<Path> collectModulePoms() {
        List<Path> poms = new ArrayList<>();
        // Root POM
        Path rootPom = projectRoot.resolve("pom.xml");
        if (Files.exists(rootPom)) {
            poms.add(rootPom);
        }
        // Module POMs
        for (String moduleDir : MODULE_DIRS) {
            Path modulePom = projectRoot.resolve(moduleDir).resolve("pom.xml");
            if (Files.exists(modulePom)) {
                poms.add(modulePom);
            }
        }
        return poms;
    }

    private List<Path> findJavaFiles(Path directory) {
        List<Path> javaFiles = new ArrayList<>();
        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.toString().endsWith(".java")) {
                        javaFiles.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            // Silently skip inaccessible directories
        }
        return javaFiles;
    }

    /**
     * Extracts the project-level version from a POM file content.
     * Handles both root POMs (version directly under project) and child POMs
     * (version may be in parent section or directly under project).
     */
    static String extractProjectVersion(String content) {
        // Remove XML comments
        String cleaned = content.replaceAll("<!--[\\s\\S]*?-->", "");

        // Strategy 1: Look for <version> directly under <project> (not inside <parent>)
        // Remove the <parent>...</parent> block first, then find <version>
        String withoutParent = cleaned.replaceAll("<parent>[\\s\\S]*?</parent>", "");
        // Also remove <dependencies> and <dependencyManagement> blocks
        String withoutDeps = withoutParent
                .replaceAll("<dependencyManagement>[\\s\\S]*?</dependencyManagement>", "")
                .replaceAll("<dependencies>[\\s\\S]*?</dependencies>", "")
                .replaceAll("<build>[\\s\\S]*?</build>", "")
                .replaceAll("<profiles>[\\s\\S]*?</profiles>", "")
                .replaceAll("<properties>[\\s\\S]*?</properties>", "");

        java.util.regex.Matcher m = Pattern.compile("<version>([^<]+)</version>").matcher(withoutDeps);
        if (m.find()) {
            return m.group(1).trim();
        }

        // Strategy 2: If no direct version, get it from <parent> block
        java.util.regex.Matcher parentMatcher = Pattern.compile("<parent>[\\s\\S]*?<version>([^<]+)</version>[\\s\\S]*?</parent>").matcher(cleaned);
        if (parentMatcher.find()) {
            return parentMatcher.group(1).trim();
        }

        return null;
    }
}
