package io.protop.gradle.plugin

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Exec

import java.nio.file.Path
import java.nio.file.Paths

class ProtopGradlePlugin implements Plugin<Project> {
    @Override
    void apply(final Project project) {
        project.with {
            afterEvaluate {
                try {
                    protobuf {
                        generateProtoTasks {
                            all().each { task ->
                                task.dependsOn { protopSync }
                            }
                        }
                    }

                    sourceSets {
                        main {
                            proto {
                                srcDirs += protop.path
                            }
                        }
                    }
                } catch (final Exception e) {
                    throw e.getMessage().contains("Could not find method protobuf()")
                            ? new GradleException('`io.protop` Gradle plugin needs project to apply `com.google.protobuf` Gradle plugin.')
                            : e
                }
            }
        }

        final ProtopExtension extension = project.getExtensions().create(
                'protop', ProtopExtension.class)

        registerProtopClean(project)
        registerProtopSync(project, extension)
    }

    private void registerProtopClean(final Project project) {
        project.getTasks().register('protopClean', Delete.class, { delete ->
            protopClean(project, delete)
        })
    }

    private void protopClean(final Project project, final Delete delete) {
        final Path path = Paths.get(
                project.getProjectDir().getAbsolutePath(),
                ProtopConstants.PROTOP_DIR_NAME)
        delete.delete(path)
    }

    private void registerProtopSync(final Project project, final ProtopExtension extension) {
        project.getTasks().register('protopSync', Exec.class, { exec ->
            protopSync(exec, extension)

            exec.doLast { s ->
                System.out.println('Protop sync succeeded')
            }
        })
    }

    private void protopSync(final Exec exec, final ProtopExtension extension) {
        final String protopHome = System.getenv('PROTOP_HOME')
        final String protopPrefix = protopHome != null ?
            protopHome + '/bin/'
            : ''

        final List<String> args = new ArrayList<>()

        args.add(protopPrefix + 'protop')
        args.add('sync')

        if (extension.refreshGitSources) {
            args.add('--git-refresh')
        }

        if (extension.useLinks) {
            args.add('--use-links')
        }

        exec.commandLine(args)
    }
}
