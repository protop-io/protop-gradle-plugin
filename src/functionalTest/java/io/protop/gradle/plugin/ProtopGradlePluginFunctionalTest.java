package io.protop.gradle.plugin;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;

@ExtendWith(SoftAssertionsExtension.class)
public class ProtopGradlePluginFunctionalTest {
  private GradleRunner gradleRunner;

  private Path rootDir;

  @BeforeEach
  public void beforeEach(@TempDir final Path rootDir) throws Exception {
    this.gradleRunner = GradleRunner.create();

    this.rootDir = rootDir;
    final Path protopJson = rootDir.resolve("protop.json");

    // TODO(noel-yap): When `protop` supports `file` schema, replace with test data to improve test focus and locality.
    Files.writeString(protopJson, """
        {
          "dependencies": {
            "protop/pingpong": "gh:protop-io/pingpong-protos"
          }
        }
        """);

    final Path buildGradle = rootDir.resolve("build.gradle");
    Files.writeString(buildGradle, """
        plugins {
          id 'io.protop' version '1.0.0'
          id 'java'
        }
        
        task assertMainProtoSrcDirsIncludesProtopPath {
          doLast {
            assert sourceSets.main.proto.srcDirs.contains(file("${rootDir}/${protop.path}"))
          }
        }
        """);
  }

  @Test
  @DisplayName("Should sync proto files.")
  public void shouldSyncProtoFiles(final SoftAssertions softly) {
    final BuildResult buildResult = gradleRunner
        .withPluginClasspath()
        .withProjectDir(rootDir.toFile())
        .withArguments("--info", "--stacktrace", ":protopSync")
        .build();

    softly.assertThat(buildResult.getOutput())
        .contains("""
            Syncing external dependencies.
            Done syncing.
            Protop sync succeeded
            """);
    softly.assertThat(Objects.requireNonNull(buildResult.task(":protopSync")).getOutcome())
        .isEqualByComparingTo(SUCCESS);
  }

  @Test
  @DisplayName("`generateProto` task should depend on `protoSync`.")
  public void generateProtoTaskShouldIncludeProtoSync() {
    final BuildResult buildResult = gradleRunner
        .withPluginClasspath()
        .withProjectDir(rootDir.toFile())
        .withArguments("--dry-run", "--info", "--stacktrace", ":generateProto")
        .build();

    assertThat(buildResult.getOutput())
        .contains(":protopSync SKIPPED");
  }

  @Test
  @DisplayName("`sourceSets.main.proto.srcDirs` should include `protop.path`.")
  public void sourceSetsMainProtoSrcDirsShouldIncludeProtopPath(final SoftAssertions softly) {
    final BuildResult buildResult = gradleRunner
        .withPluginClasspath()
        .withProjectDir(rootDir.toFile())
        .withArguments("--info", "--stacktrace", ":assertMainProtoSrcDirsIncludesProtopPath")
        .build();

    softly.assertThat(Objects.requireNonNull(buildResult.task(":assertMainProtoSrcDirsIncludesProtopPath")).getOutcome())
        .isEqualByComparingTo(SUCCESS);
  }
}
