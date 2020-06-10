package io.protop.gradle.plugin;

public class ProtopExtension {

    public boolean refreshGitSources = false;
    public boolean useLinks = false;
    public String path = ProtopConstants.PROTOP_DIR_NAME
            + "/"
            + ProtopConstants.PROTOP_PATH_DIR_NAME;
}
