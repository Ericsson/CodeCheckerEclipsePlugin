package cc.codechecker.plugin.views.report.details;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;

import cc.codechecker.api.action.BugPathItem;

public class BugPathLabelProvider extends LabelProvider {

    @Override
    public String getText(Object element) {

        BugPathItem bpi = (BugPathItem) element;
        Path path = new Path(bpi.getFile());
        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
        
        return file.getName() + " : " + bpi.getStartPosition().getLine() + " : " + bpi.getMessage();

    }

}
