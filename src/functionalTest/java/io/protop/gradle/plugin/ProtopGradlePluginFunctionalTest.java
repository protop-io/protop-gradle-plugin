package io.protop.gradle.plugin;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.UnexpectedBuildFailure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;

@ExtendWith(SoftAssertionsExtension.class)
public class ProtopGradlePluginFunctionalTest {
  private Path rootDir;

  @BeforeEach
  public void beforeEach(@TempDir final Path rootDir) throws Exception {
    this.rootDir = rootDir;

    // TODO(noel-yap): When `protop` supports `file` schema, replace with test data to improve test focus and locality.
    final Path protopJson = rootDir.resolve("protop.json");
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
          id 'com.google.protobuf' version '0.8.+'
          id 'io.protop' version '1.0.0'
          id 'java'
        }
        """);
  }

  @Test
  @DisplayName("Should sync proto files.")
  public void shouldSyncProtoFiles(final SoftAssertions softly) {
    final BuildResult buildResult = gradle("--info", "--stacktrace", ":protopSync");

    softly.assertThat(Objects.requireNonNull(buildResult.task(":protopSync")).getOutcome())
        .isEqualByComparingTo(SUCCESS);
    softly.assertThat(buildResult.getOutput())
        .contains("""
            Syncing external dependencies.
            Done syncing.
            Protop sync succeeded
            """);
  }

  @Test
  @DisplayName("`generateProto` task should depend on `protoSync`.")
  public void generateProtoTaskShouldIncludeProtoSync() {
    final BuildResult buildResult = gradle("--dry-run", "--info", "--stacktrace", ":generateProto");

    assertThat(buildResult.getOutput())
        .contains(":protopSync SKIPPED");
  }

  @Test
  @DisplayName("`sourceSets.main.proto.srcDirs` should include `protop.path`.")
  public void sourceSetsMainProtoSrcDirsShouldIncludeProtopPath() throws Exception {
    final Path buildGradle = rootDir.resolve("build.gradle");
    Files.writeString(buildGradle, """
        plugins {
          id 'com.google.protobuf' version '0.8.+'
          id 'io.protop' version '1.0.0'
          id 'java'
        }
        
        task assertMainProtoSrcDirsIncludesProtopPath {
          doLast {
            assert sourceSets.main.proto.srcDirs.contains(file("${rootDir}/${protop.path}"))
          }
        }
        """);

    final BuildResult buildResult = gradle("--info", "--stacktrace", ":assertMainProtoSrcDirsIncludesProtopPath");

    assertThat(Objects.requireNonNull(buildResult.task(":assertMainProtoSrcDirsIncludesProtopPath")).getOutcome())
        .isEqualByComparingTo(SUCCESS);
  }

  @Test
  @DisplayName("Build should fail with actionable error if `protobuf` Gradle plugin isn't applied.")
  public void buildShouldFailWithActionableErrorIfProtobufGradlePluginIsnTApplied()
      throws Exception {
    final Path buildGradle = rootDir.resolve("build.gradle");
    Files.writeString(buildGradle, """
        plugins {
          id 'io.protop' version '1.0.0'
          id 'java'
        }
        """);

    assertThatThrownBy(() -> gradle("--info", "--stacktrace", "tasks"))
        .isInstanceOf(UnexpectedBuildFailure.class)
        .hasMessageContaining("`io.protop` Gradle plugin needs project to apply `com.google.protobuf` Gradle plugin.");
  }

  private BuildResult gradle(final String... args) {
    final GradleRunner gradleRunner = GradleRunner.create();

    return gradleRunner
        .withPluginClasspath()
        .withProjectDir(rootDir.toFile())
        .withArguments(args)
        .build();
  }
}
