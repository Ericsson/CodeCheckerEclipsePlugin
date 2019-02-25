package org.codechecker.eclipse.plugin.config.global;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.codechecker.eclipse.plugin.config.CommonGui;

import org.codechecker.eclipse.plugin.Logger;
import org.eclipse.core.runtime.IStatus;

public class CcGlobalProperties extends PreferencePage implements IWorkbenchPreferencePage {

    private static CommonGui commonGUI;

    public CcGlobalProperties(){
        super();

        commonGUI=new CommonGui();
    }
    @Override
    protected Control createContents(final Composite parent) {

        return commonGUI.createContents(parent);
    }

    @Override
    public void performDefaults() {

        commonGUI.performDefaults();
        super.performDefaults();

    }

    @Override
    public boolean isValid() {

        return true;
    }

    @Override
    public boolean performOk() {

        commonGUI.performOk();
        return super.performOk();
    }

    @Override
    public void init(IWorkbench workbench) {
        Logger.log(IStatus.INFO, "init called");
        // TODO Auto-generated method stub

    }
}
