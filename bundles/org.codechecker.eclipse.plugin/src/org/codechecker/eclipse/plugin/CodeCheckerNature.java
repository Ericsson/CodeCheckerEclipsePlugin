package org.codechecker.eclipse.plugin;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import org.codechecker.eclipse.plugin.config.CcConfiguration;
import org.codechecker.eclipse.plugin.config.CodeCheckerContext;

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
    	CcConfiguration config = new CcConfiguration(project);
    	config.modifyProjectEnvironmentVariables();
    	CodeCheckerContext.getInstance().setConfig(project, config);
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
