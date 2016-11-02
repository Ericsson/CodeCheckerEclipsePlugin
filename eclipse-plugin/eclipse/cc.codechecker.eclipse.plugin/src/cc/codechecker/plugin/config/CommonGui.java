package cc.codechecker.plugin.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import com.google.common.base.Optional;

import cc.codechecker.api.runtime.CodeCheckEnvironmentChecker;
import cc.codechecker.plugin.config.CcConfiguration;
import cc.codechecker.api.config.Config.ConfigTypes;

import cc.codechecker.plugin.config.project.CcProjectProperties;
import cc.codechecker.plugin.itemselector.CheckerView;
import cc.codechecker.plugin.utils.CheckerItem;
import cc.codechecker.plugin.utils.CheckerItem.LAST_ACTION;

public class CommonGui {

	// Logger
	private static final Logger logger = LogManager.getLogger(CommonGui.class);

	boolean global;//whether this class is for global or project specific preferences
	boolean useGlobalSettings;//if this is project specific page, wheter to use global preferences 
	IProject project;
	private  Text codeCheckerDirectoryField;// codechecker dir
	private  Text pythonEnvField;// CodeChecker python env
	private  Text numThreads;// #of analysis threads
	private  Text cLoggers;// #C compiler commands to catch
	
	private String checkerListArg = "";
	private ScrolledForm form;

	private Button globalcc;
    private Button projectcc;
    //CodeCheckEnvironmentChecker checkerEnv=null;
    //needs to be updated when codechecker dir or python env changes
    
	public CommonGui(){		
		global=true;
	}
	public CommonGui(IProject proj){		
		project=proj;
		global=false;
	}	
	
	protected Text addTextField(FormToolkit toolkit, Composite comp, String labelText, String def) {
		Text ret;
		Label label = toolkit.createLabel(comp, labelText);
		label.setLayoutData(new GridData());
		ret = toolkit.createText(comp, def);
		ret.setLayoutData(new GridData(GridData.FILL));
		return ret;
	}
	
	public Control createContents(final Composite parent) {
		logger.log(Level.DEBUG, "createContents called");		
		final FormToolkit toolkit = new FormToolkit(parent.getDisplay());

		form = toolkit.createScrolledForm(parent);
		form.getBody().setLayout(new GridLayout());
		Section  globalConfigSection=null;		
		if (!global){					
			globalConfigSection = toolkit.createSection(form.getBody(), ExpandableComposite.EXPANDED);			
		}
		final Section checkerConfigSection = toolkit.createSection(form.getBody(),
				ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
		checkerConfigSection.setEnabled(true);
		
		final Composite client = toolkit.createComposite(checkerConfigSection);
		client.setLayout(new GridLayout(3,false));
		checkerConfigSection.setClient(client);
		checkerConfigSection.setText("Configuration");

		codeCheckerDirectoryField = addTextField(toolkit, client, "CodeChecker package root directory", "");
		codeCheckerDirectoryField.addListener(SWT.FocusOut, new Listener() {
			@Override
			public void handleEvent(Event event) {
				try {
					Map<ConfigTypes, String> changedConfig=getConfig();
					CodeCheckEnvironmentChecker checkerEnv= new CodeCheckEnvironmentChecker(changedConfig);
					form.setMessage("CodeChecker package directory is valid", 1);
				} catch (IllegalArgumentException e1) {
					form.setMessage("CodeChecker package directory is invalid", 3);					
				}
			}
		});

		final Button codeCheckerDirectoryFieldBrowse = new Button(client, SWT.PUSH);
		codeCheckerDirectoryFieldBrowse.setText("Browse");
		codeCheckerDirectoryFieldBrowse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				DirectoryDialog dlg = new DirectoryDialog(client.getShell());
				dlg.setFilterPath(codeCheckerDirectoryField.getText());
				dlg.setText("Browse codechecker root");
				String dir = dlg.open();
				if (dir != null) {
					codeCheckerDirectoryField.setText(dir);
					try {
						Map<ConfigTypes, String> changedConfig=getConfig();
						CodeCheckEnvironmentChecker checkerEnv= new CodeCheckEnvironmentChecker(changedConfig);
						form.setMessage("CodeChecker package directory is valid", 1);
					} catch (IllegalArgumentException e1) {
						form.setMessage("CodeChecker package directory is invalid", 3);
					}
				}
			}
		});

		pythonEnvField = addTextField(toolkit, client, "Python virtualenv root directory (optional)", "");
		final Button pythonEnvFieldBrowse = new Button(client, SWT.PUSH);
		pythonEnvFieldBrowse.setText("Browse");
		pythonEnvFieldBrowse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				DirectoryDialog dlg = new DirectoryDialog(client.getShell());
				dlg.setFilterPath(codeCheckerDirectoryField.getText());
				dlg.setText("Browse python environment");
				String dir = dlg.open();
				if (dir != null) {
					pythonEnvField.setText(dir);
				}
			}
		});

		numThreads = addTextField(toolkit, client, "Number of analysis threads", "4");
		toolkit.createLabel(client, "");
		cLoggers = addTextField(toolkit, client, "Compiler commands to log", "gcc:g++:clang");
		toolkit.createLabel(client, "");
		
		Map<ConfigTypes, String> config=loadConfig(false);
		try {			
			CodeCheckEnvironmentChecker checkerEnv= new CodeCheckEnvironmentChecker(config);
			form.setMessage("CodeChecker package directory is valid", 1);
		} catch (Exception e1) {
			form.setMessage("CodeChecker package directory is invalid", 3);					
		}
	
		final Button checkers = toolkit.createButton(client, "Toggle enabled checkers", SWT.PUSH);
		checkers.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				Action action = new Action() {
					@Override
					public void run() {
						Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
						Map<ConfigTypes, String> config = getConfig();
						try{
							CodeCheckEnvironmentChecker checkerEnv = new CodeCheckEnvironmentChecker(config);
							ArrayList<CheckerItem> checkersList=getCheckerList(checkerEnv);
							logger.log(Level.DEBUG, "checker list was "+checkerListArg);
							CheckerView dialog = new CheckerView(activeShell, checkersList);
	
							int result = dialog.open();
	
							if (result == 0) {
								checkerListArg=checkerListToCheckerListArg(dialog.getCheckersList());
								logger.log(Level.DEBUG, "checker list set to "+checkerListArg);							
							}
						}catch(IllegalArgumentException e){
							logger.log(Level.DEBUG, "Codechecker environment is invalid"+e);							
						}						
					}
				};
				action.run();
			}
		});
		if (!global){										
			checkerConfigSection.setEnabled(!useGlobalSettings);			
			final Composite client3 = toolkit.createComposite(globalConfigSection);
			client3.setLayout(new GridLayout(2, true));
			globalConfigSection.setClient(client3);
			globalcc = toolkit.createButton(client3, "Use global configuration", SWT.RADIO);
			globalcc.setSelection(useGlobalSettings);
			globalcc.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					checkerConfigSection.setEnabled(false);
					useGlobalSettings=true;
				}
			});
			projectcc = toolkit.createButton(client3, "Use project configuration", SWT.RADIO);
			projectcc.setSelection(!useGlobalSettings);
			projectcc.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					checkerConfigSection.setEnabled(true);
					useGlobalSettings=false;
				}
			});

		}
		return form.getBody();
	}

	private Optional<String> getPythonEnv() {
		String s = this.pythonEnvField.getText();
		if (s.isEmpty()) {
			return Optional.absent();
		} else {
			return Optional.of(s);
		}
	}

	private ArrayList<CheckerItem> getCheckerList(CodeCheckEnvironmentChecker ccec) {
		// ArrayList<CheckerItem> defaultCheckersList = new ArrayList<>();
		ArrayList<CheckerItem> checkersList = new ArrayList<>(); //
		// new Checkers List
		String s = ccec.getCheckerList();
		String[] newCheckersSplit = s.split("\n");
		// old Checkers Command
		String[] checkersCommand = checkerListArg.split(" ");
		List<String> oldCheckersCommand = Arrays.asList(checkersCommand);
		for (String it : newCheckersSplit) {
			String checkerName = it.split(" ")[2];
			CheckerItem check = new CheckerItem(checkerName);
			boolean defaultEnabled = false;

			if (it.split(" ")[1].equals("+"))
				defaultEnabled = true;
			if (defaultEnabled) {
				if (checkerListArg.contains(" -d " + checkerName)) {
					check.setLastAction(LAST_ACTION.DESELECTION);
				} else {
					check.setLastAction(LAST_ACTION.SELECTION);
				}
			} else {
				if (checkerListArg.contains(" -e " + checkerName)) {
					check.setLastAction(LAST_ACTION.SELECTION);
				} else {
					check.setLastAction(LAST_ACTION.DESELECTION);
				}
			}
			checkersList.add(check);
		}		
		return checkersList;
	}

	protected String checkerListToCheckerListArg(List<CheckerItem> chl) {
		String checkerListArg="";
		for (int i = 0; i < chl.size(); ++i) {
			if (chl.get(i).getLastAction()==LAST_ACTION.SELECTION) 
				checkerListArg+=(" -e " + chl.get(i).getText() + " ");
			else
				checkerListArg+=(" -d " + chl.get(i).getText() + " ");									
		}		
		return checkerListArg;		
	}

	public Map<ConfigTypes, String> loadConfig(boolean resetToDefault) {
		Map<ConfigTypes, String> config;		
		if (!resetToDefault){
		if (global)			
			config = CcConfiguration.getGlobalConfig();
		else{			
	        CcConfiguration c=new CcConfiguration(project);
			config = c.getProjectConfig();
			useGlobalSettings = config.get(ConfigTypes.IS_GLOBAL).equals("true");
			if (useGlobalSettings)
				config=CcConfiguration.getGlobalConfig();
		}}
		else
			config=CcConfiguration.getDefaultConfig();
		
		codeCheckerDirectoryField.setText(config.get(ConfigTypes.CHECKER_PATH));
		pythonEnvField.setText(config.get(ConfigTypes.PYTHON_PATH));
		checkerListArg = config.get(ConfigTypes.CHECKER_LIST);
		cLoggers.setText(config.get(ConfigTypes.COMPILERS));
		numThreads.setText(config.get(ConfigTypes.ANAL_THREADS));
		return config;
	}
	
	public Map<ConfigTypes, String> getConfig() {				
		Map<ConfigTypes, String> config;		
		if (global){			
			config = CcConfiguration.getGlobalConfig();
		}
		else{
			CcConfiguration c=new CcConfiguration(project);
			config = c.getProjectConfig();
		}
		
		config.put(ConfigTypes.CHECKER_PATH, codeCheckerDirectoryField.getText());
		config.put(ConfigTypes.PYTHON_PATH, pythonEnvField.getText());
		config.put(ConfigTypes.CHECKER_LIST, checkerListArg);
		config.put(ConfigTypes.ANAL_THREADS, numThreads.getText());
		config.put(ConfigTypes.COMPILERS, cLoggers.getText());
		return config;
	}

	public void saveConfig() {				
		Map<ConfigTypes, String> config;		
		if (global){			
			config = CcConfiguration.getGlobalConfig();
		}
		else{
			CcConfiguration c=new CcConfiguration(project);
			config = c.getProjectConfig();
		}
		
		config.put(ConfigTypes.CHECKER_PATH, codeCheckerDirectoryField.getText());
		config.put(ConfigTypes.PYTHON_PATH, pythonEnvField.getText());
		config.put(ConfigTypes.CHECKER_LIST, checkerListArg);
		config.put(ConfigTypes.ANAL_THREADS, numThreads.getText());
		config.put(ConfigTypes.COMPILERS, cLoggers.getText());
		if (global)
			CcConfiguration.updateGlobal(config);
		else{
			String g="true";
			if (!useGlobalSettings)
				g="false";			
			config.put(ConfigTypes.IS_GLOBAL, g);
			logger.log(Level.DEBUG, "Saving project settings: IS_GLOBAL:"+g);
			CcConfiguration c=new CcConfiguration(project);
			c.updateProject(config);
		}			
	}


	public void performDefaults() {
		loadConfig(true);
	}
	
	public boolean isValid() {
		return true;
	}

	
	public void performOk() {
		logger.log(Level.INFO, "SERVER_GUI_MSG >> Saving!");
		saveConfig();
		
	}

	
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}
}
