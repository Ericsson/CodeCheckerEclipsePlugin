package cc.codechecker.plugin.config.project;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import cc.codechecker.plugin.config.CommonGui;
import org.apache.log4j.Logger;


import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

public class CcProjectProperties extends PropertyPage implements IWorkbenchPropertyPage {

    //Logger
    private static final Logger logger = LogManager.getLogger(CcProjectProperties.class);
 	private CommonGui commonGUI;

	public CcProjectProperties(){
		super();
		logger.log(Level.DEBUG, "constructor called");
	}
   
    @Override
    protected Control createContents(Composite parent) {
		logger.log(Level.DEBUG, "createcontents called");
		IProject project = (IProject) this.getElement().getAdapter(IProject.class);
		commonGUI=new CommonGui(project);
		return commonGUI.createContents(parent);
    }

    
    @Override
    public void performDefaults() {
		logger.log(Level.DEBUG, "performDefautls called");
		if (commonGUI!=null)
			commonGUI.performDefaults();
		super.performDefaults();
    }

    @Override
    public boolean isValid() {
		logger.log(Level.DEBUG, "isvalid called");
		return true;
    }

    @Override
    public boolean performOk() {
		logger.log(Level.DEBUG, "performok called");
		logger.log(Level.INFO, "SERVER_GUI_MSG >> Saving!");
		if (commonGUI!=null)
			commonGUI.performOk();
		return super.performOk();
    }
}
