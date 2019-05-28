package org.codechecker.eclipse.plugin.utils;

import java.nio.file.Path;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

/**
 * Import projects into eclipse with this class.
 */
public class ProjectImporter {
    
    /**
     * Hidden utility ctor.
     */
    private ProjectImporter() {}

    /**
     * Imports a project into workspace.
     * https://www.eclipse.org/forums/index.php/t/560903/
     *
     * @param projectFile
     *            The project file to be imported.
     * @param projectName
     *            The project name that will be used to create the project
     * @throws CoreException
     *             Project cannot be created: if this method fails. Reasons include:
     *             - This project already exists in the workspace. - The name of
     *             this resource is not valid (according to
     *             IWorkspace.validateName). - The project location is not valid
     *             (according to IWorkspace.validateProjectLocation). - The project
     *             description file could not be created in the project content
     *             area. - Resource changes are disallowed during certain types of
     *             resource change event notification. See IResourceChangeEvent for
     *             more details. .project file has troubles. Reasons include: - The
     *             project description file does not exist. - The file cannot be
     *             opened or read. - The file cannot be parsed as a legal project
     *             description. or during opening - Resource changes are disallowed
     *             during certain types of resource change event notification. See
     *             IResourceChangeEvent for more details.
     */
    public static void importProject(final Path projectFile, final String projectName) throws CoreException {
        IProjectDescription description = ResourcesPlugin.getWorkspace()
                .loadProjectDescription(new org.eclipse.core.runtime.Path(projectFile.toFile().getAbsolutePath()));
        description.setName(projectName);
        create(description, projectName);
    }
    
    /**
     * Convenience method for importing projects with names specified 
     * from the targeted project file.
     * Imports a project into workspace.
     * https://www.eclipse.org/forums/index.php/t/560903/
     *
     * @param projectFile
     *            The project file to be imported.
     * @throws CoreException
     *             Project cannot be created: if this method fails. Reasons include:
     *             - This project already exists in the workspace. - The name of
     *             this resource is not valid (according to
     *             IWorkspace.validateName). - The project location is not valid
     *             (according to IWorkspace.validateProjectLocation). - The project
     *             description file could not be created in the project content
     *             area. - Resource changes are disallowed during certain types of
     *             resource change event notification. See IResourceChangeEvent for
     *             more details. .project file has troubles. Reasons include: - The
     *             project description file does not exist. - The file cannot be
     *             opened or read. - The file cannot be parsed as a legal project
     *             description. or during opening - Resource changes are disallowed
     *             during certain types of resource change event notification. See
     *             IResourceChangeEvent for more details.
     */
    public static void importProject(final Path projectFile) throws CoreException {
        IProjectDescription description = ResourcesPlugin.getWorkspace()
                .loadProjectDescription(new org.eclipse.core.runtime.Path(projectFile.toFile().getAbsolutePath()));
        create(description, description.getName());
    }
    
    /**
     * The import happens here.
     * @param description Description passed in.
     * @param projectName The actual project name.
     * @throws CoreException Same as above.
     */
    public static void create(final IProjectDescription description, final String projectName) throws CoreException {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        if (!project.exists())
            project.create(description, null);
        project.open(null);
    }
}
