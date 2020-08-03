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

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;

@ExtendWith(SoftAssertionsExtension.class)
public class ProtopGradlePluginFunctionalTest {
  private GradleRunner gradleRunner;

  @BeforeEach
  public void beforeEach() {
    gradleRunner = GradleRunner.create();
  }

  @Test
  @DisplayName("Should sync proto files.")
  public void shouldSyncProtoFiles(
      final SoftAssertions softly,
      @TempDir final Path buildDir) throws Exception {
    final Path protopJson = buildDir.resolve("protop.json");
    // TODO(noel-yap): When `protop` supports `file` schema, replace with test data to improve test focus and locality.
    Files.writeString(protopJson, """
        {
          "dependencies": {
            "protop/pingpong": "gh:protop-io/pingpong-protos"
          }
        }
        """);

    final Path buildGradle = buildDir.resolve("build.gradle");
    Files.writeString(buildGradle, """
        plugins {
          id 'io.protop' version '1.0.0'
        }
        """);

    final BuildResult result = gradleRunner
        .withPluginClasspath()
        .withProjectDir(buildDir.toFile())
        .withArguments("protopSync")
        .build();

    softly.assertThat(result.getOutput())
        .contains("""
            Syncing external dependencies.
            Done syncing.
            Protop sync succeeded
            """);
    softly.assertThat(result.task(":protopSync").getOutcome())
        .isEqualByComparingTo(SUCCESS);
  }
}
