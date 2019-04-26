package org.codechecker.eclipse.plugin.config.project;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.codechecker.eclipse.plugin.config.CommonGui;

import org.codechecker.eclipse.plugin.Logger;
import org.eclipse.core.runtime.IStatus;

public class CcProjectProperties extends PropertyPage implements IWorkbenchPropertyPage {

    private CommonGui commonGUI;

    public CcProjectProperties(){
        super();
    }

    @Override
    protected Control createContents(Composite parent) {
        IProject project = (IProject) this.getElement().getAdapter(IProject.class);
        commonGUI=new CommonGui(project);
        return commonGUI.createContents(parent);
    }


    @Override
    public void performDefaults() {
        if (commonGUI!=null)
            commonGUI.performDefaults();
        super.performDefaults();
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean performOk() {
        if (commonGUI!=null)
            commonGUI.performOk();
        return super.performOk();
    }
}
