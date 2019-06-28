package org.codechecker.eclipse.plugin;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import org.codechecker.eclipse.plugin.config.CodeCheckerContext;
import org.codechecker.eclipse.plugin.config.project.CodeCheckerProject;

/**
 * Eclipse uses natures as project feature indicators.
 * This class adds CodeChecker related nature.
 *
 */
public class CodeCheckerNature implements IProjectNature {

    public static final String NATURE_ID = "org.codechecker.eclipse.plugin.CodeCheckerNature";
    IProject project; 

    @Override
    public void configure() throws CoreException {
        CodeCheckerProject cCProject = new CodeCheckerProject(project);
        cCProject.modifyProjectEnvironmentVariables();
        CodeCheckerContext.getInstance().addCodeCheckerProject(cCProject);
    }

    @Override
    public void deconfigure() throws CoreException {}

    @Override
    public IProject getProject() {
        return project;
    }

    @Override
    public void setProject(IProject project) {
        this.project = project;
    }

}
