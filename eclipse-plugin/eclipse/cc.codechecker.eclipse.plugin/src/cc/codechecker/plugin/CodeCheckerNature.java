package cc.codechecker.plugin;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import cc.codechecker.plugin.config.CodeCheckerContext;
import cc.codechecker.plugin.runtime.CodecheckerServerThread;

public class CodeCheckerNature implements IProjectNature {

    public static final String NATURE_ID = "cc.codechecker.plugin.CodeCheckerNature";
    IProject project; 

    @Override
    public void configure() throws CoreException {
        CodecheckerServerThread server = CodeCheckerContext.getInstance().getServerObject(project);
    }

    @Override
    public void deconfigure() throws CoreException {
        // TODO Auto-generated method stub

    }

    @Override
    public IProject getProject() {
        return project;
    }

    @Override
    public void setProject(IProject project) {
        this.project = project;
    }

}
