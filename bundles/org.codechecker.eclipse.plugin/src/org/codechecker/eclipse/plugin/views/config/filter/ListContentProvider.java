package org.codechecker.eclipse.plugin.views.config.filter;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.codechecker.eclipse.plugin.Logger;
import org.eclipse.core.runtime.IStatus;

public class ListContentProvider implements IContentProvider, IStructuredContentProvider {
	
    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof List) {
            Logger.log(IStatus.INFO, "SERVER_GUI_MSG >> Displaying list");
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
