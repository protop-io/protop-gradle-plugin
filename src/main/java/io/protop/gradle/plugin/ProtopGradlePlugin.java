package io.protop.gradle.plugin;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.tasks.Delete;
import org.gradle.api.tasks.Exec;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ProtopGradlePlugin implements Plugin<Project> {

    public void apply(Project project) {
        ProtopExtension extension = project.getExtensions().create(
                "protop", ProtopExtension.class);

        registerProtopClean(project);
        registerProtopSync(project, extension);
    }

    private void registerProtopClean(Project project) {
        project.getTasks().register("protopClean", Delete.class, delete -> {
            protopClean(project, delete);
        });
    }

    private void protopClean(Project project, Delete delete) {
        Path path = Paths.get(
                project.getProjectDir().getAbsolutePath(),
                ProtopConstants.PROTOP_DIR_NAME);
        delete.delete(path);
    }

    private void registerProtopSync(Project project, ProtopExtension extension) {
        project.getTasks().register("protopSync", Exec.class, exec -> {
            protopSync(exec, extension);

            exec.doLast(s -> System.out.println("Protop sync succeeded"));
        });
    }

    private void protopSync(Exec exec, ProtopExtension extension) {
        List<String> args = new ArrayList<>();
        args.add("protop");
        args.add("sync");
        if (extension.refreshGitSources) args.add("--git-refresh");
        if (extension.useLinks) args.add("--use-links");
        exec.commandLine(args);
    }
}
