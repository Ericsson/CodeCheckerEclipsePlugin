package cc.codechecker.plugin.config.global;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.apache.log4j.LogManager;

import org.apache.log4j.Level;
import cc.codechecker.plugin.config.CommonGui;
import cc.codechecker.plugin.config.project.CcProjectProperties;


public class CcGlobalProperties extends PreferencePage implements IWorkbenchPreferencePage {

	// Logger
	private static final Logger logger = LogManager.getLogger(CcGlobalProperties.class);
	private static CommonGui commonGUI;

	public CcGlobalProperties(){
		super();
		logger.log(Level.DEBUG, "constructor called");
		commonGUI=new CommonGui();
	}
	@Override
	protected Control createContents(final Composite parent) {
		logger.log(Level.DEBUG, "createcontents called");
		return commonGUI.createContents(parent);
	}

	@Override
	public void performDefaults() {
		logger.log(Level.DEBUG, "performDefautls called");
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
		commonGUI.performOk();
		return super.performOk();
	}

	@Override
	public void init(IWorkbench workbench) {
		logger.log(Level.DEBUG, "init called");
		// TODO Auto-generated method stub

	}
}
