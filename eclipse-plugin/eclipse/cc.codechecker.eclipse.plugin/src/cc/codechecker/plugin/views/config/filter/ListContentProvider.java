package cc.codechecker.plugin.views.config.filter;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import cc.codechecker.api.runtime.ShellExecutorHelper;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.apache.log4j.Level;

public class ListContentProvider implements IContentProvider, IStructuredContentProvider {

	private static final Logger logger = LogManager.getLogger(ListContentProvider.class.getName());
	
    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof List) {
            logger.log(Level.DEBUG, "SERVER_GUI_MSG >> Displaying list");
            return ((List) inputElement).toArray();
        }
        return ArrayUtils.toArray();
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

}
