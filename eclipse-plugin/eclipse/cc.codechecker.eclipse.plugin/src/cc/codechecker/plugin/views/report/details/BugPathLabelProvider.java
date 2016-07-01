package cc.codechecker.plugin.views.report.details;

import org.eclipse.jface.viewers.LabelProvider;

import cc.codechecker.api.action.BugPathItem;

public class BugPathLabelProvider extends LabelProvider {

    @Override
    public String getText(Object element) {
        BugPathItem bpi = (BugPathItem) element;

        String file = bpi.getFile();
        String[] parts = file.split("/");

        return parts[parts.length - 1] + " : " + bpi.getStartPosition().getLine() + " : " + bpi.getMessage();

    }

}
