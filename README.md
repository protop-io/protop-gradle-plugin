# Gradle Plugin for Protop

This plugin wraps the protop CLI for your Gradle project.
For more information about the protop CLI, please refer to the website at [protop.io](https://protop.io).

Prerequisites:
- Latest version of protop

## Current version

The current release is version 0.2.0. Import it as a buildscript dependency:

```groovy
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "io.protop:protop-gradle-plugin:0.2.0"
    }
}
```

Apply the plugin, and customize the options:

```groovy
apply plugin: "io.protop"
protop {
    useLinks = true
    refreshGitSources = true
}
```

Add the protop path to your sources. Without this plugin, you'd do the same but
with the raw path. Here, the plugin provides that value as a variable:

```groovy
sourceSets {
    main {
        proto {
            srcDirs += protop.path
        }
    }
}
```

Finally, have the [Protobuf plugin](https://github.com/google/protobuf-gradle-plugin) depend on the `protopSync` task:

```groovy
protobuf {
    // ...
    generateProtoTasks {
        all().each { task ->
            task.dependsOn { protopSync }
        }
    }
}
```

## Examples

[Simple "numbers" service](https://github.com/protop-io/numbers-service)
