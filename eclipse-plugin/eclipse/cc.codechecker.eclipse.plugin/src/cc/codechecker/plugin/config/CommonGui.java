package cc.codechecker.plugin.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.StyledEditorKit.BoldAction;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.CellEditor.LayoutData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.google.common.base.Optional;

import cc.codechecker.plugin.runtime.CodeCheckEnvironmentChecker;
import cc.codechecker.plugin.config.CcConfiguration;
import cc.codechecker.plugin.config.Config.ConfigTypes;
import cc.codechecker.plugin.config.Config.ConfigTypesCommon;
import cc.codechecker.plugin.config.Config.ConfigTypesProject;
import cc.codechecker.plugin.itemselector.CheckerView;
import cc.codechecker.plugin.utils.CheckerItem;
import cc.codechecker.plugin.utils.CheckerItem.LAST_ACTION;

import cc.codechecker.plugin.Logger;
import org.eclipse.core.runtime.IStatus;

public class CommonGui {


	boolean globalGui;//whether this class is for global or project specific preferences
	boolean useGlobalSettings;//if this is project specific page, whether to use global preferences 
	IProject project;
	CcConfiguration config;
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
		globalGui=true;
	}
	public CommonGui(IProject proj){		
		project=proj;
		config = CodeCheckerContext.getInstance().getConfigForProject(proj);
		useGlobalSettings = config.isGlobal();
		globalGui=false;		       
	}	
	
	protected Text addTextField(FormToolkit toolkit, Composite comp, String labelText, String def) {
		Text ret;
		Label label = toolkit.createLabel(comp, labelText);
		label.setLayoutData(new GridData());
		ret = toolkit.createText(comp, def, SWT.MULTI|SWT.WRAP|SWT.BORDER);
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint = 200;
		ret.setLayoutData(gd);
		return ret;
	}
	
	public Control createContents(final Composite parent) {
		final FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		
		GridData ld = new GridData();
		ld.verticalAlignment = GridData.FILL;
		ld.horizontalAlignment = GridData.FILL;
		ld.grabExcessHorizontalSpace = true;
		ld.grabExcessVerticalSpace = true;
		form.setLayoutData(ld);
		
		ColumnLayout layout = new ColumnLayout();
		layout.maxNumColumns = 1 ;
		form.getBody().setLayout(layout);
		
		Section  globalConfigSection=null;		
		if (!globalGui){					
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
					Map<ConfigTypes, String> changedConfig=getConfigFromFields();
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
						Map<ConfigTypes, String> changedConfig=getConfigFromFields();
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
		
		Map<ConfigTypes, String> configMap=loadConfig(false);
		try {			
			CodeCheckEnvironmentChecker checkerEnv= new CodeCheckEnvironmentChecker(configMap);
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
						Map<ConfigTypes, String> config = getConfigFromFields();
						try{
							CodeCheckEnvironmentChecker checkerEnv = new CodeCheckEnvironmentChecker(config);
							ArrayList<CheckerItem> checkersList=getCheckerList(checkerEnv);
							CheckerView dialog = new CheckerView(activeShell, checkersList);
	
							int result = dialog.open();
	
							if (result == 0) {
								checkerListArg=checkerListToCheckerListArg(dialog.getCheckersList());
							}
						}catch(IllegalArgumentException e){
							Logger.log(IStatus.INFO, "Codechecker environment is invalid"+e);							
						}						
					}
				};
				action.run();
			}
		});
		if (!globalGui){									
			useGlobalSettings = config.isGlobal();
			recursiveSetEnabled(checkerConfigSection, !useGlobalSettings);
			final Composite client3 = toolkit.createComposite(globalConfigSection);
			client3.setLayout(new GridLayout(2, true));
			globalConfigSection.setClient(client3);
			globalcc = toolkit.createButton(client3, "Use global configuration", SWT.RADIO);
			globalcc.setSelection(useGlobalSettings);
			globalcc.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					recursiveSetEnabled(checkerConfigSection, false);
					useGlobalSettings=true;
					setFields(config.getProjectConfig(useGlobalSettings));
				}
			});
			projectcc = toolkit.createButton(client3, "Use project configuration", SWT.RADIO);
			projectcc.setSelection(!useGlobalSettings);
			projectcc.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					recursiveSetEnabled(checkerConfigSection, true);
					useGlobalSettings=false;
					setFields(config.getProjectConfig(useGlobalSettings));
				}
			});

		}
		return form.getBody();
	}
	
	/**
	 * Recursive control state modifier.
	 * If the control is {@link Composite}m toggles it state and all of it's children {@link Control}.
	 * @param control The parent control.
	 * @param b The state to be set.
	 */
	public void recursiveSetEnabled(Control control, Boolean b){
		if (control instanceof Composite) {
			Composite comp = (Composite) control;
			for (Control c : comp.getChildren())
				recursiveSetEnabled(c, b);
		} else {
			control.setEnabled(b);
		}
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
			//String checkerName = it.split(" ")[2];
			String checkerName = it;
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
	    Map<ConfigTypes, String> ret = null;		
	    if (!resetToDefault){
	    	if (globalGui)
	    		ret = CcConfiguration.getGlobalConfig();
	    	else {
	    		ret = config.getProjectConfig(null);
	    	}
	    }
	    else
	        ret=config.getDefaultConfig();

	    setFields(ret);
	    return ret;
	}
	
	/**
	 * Sets the form fields with the given config maps values.
	 * @param config The config which values are taken.
	 */
	public void setFields(Map<ConfigTypes, String> config) {
		codeCheckerDirectoryField.setText(config.get(ConfigTypesCommon.CHECKER_PATH));
	    pythonEnvField.setText(config.get(ConfigTypesCommon.PYTHON_PATH));
	    checkerListArg = config.get(ConfigTypesCommon.CHECKER_LIST);
	    cLoggers.setText(config.get(ConfigTypesCommon.COMPILERS));
	    numThreads.setText(config.get(ConfigTypesCommon.ANAL_THREADS));
	}
	
	
	
	public Map<ConfigTypes, String> getConfigFromFields() {				
		Map<ConfigTypes, String> conf = new HashMap<>();
		conf.put(ConfigTypesCommon.CHECKER_PATH, codeCheckerDirectoryField.getText());
		conf.put(ConfigTypesCommon.PYTHON_PATH, pythonEnvField.getText());
		conf.put(ConfigTypesCommon.CHECKER_LIST, checkerListArg);
		conf.put(ConfigTypesCommon.ANAL_THREADS, numThreads.getText());
		conf.put(ConfigTypesCommon.COMPILERS, cLoggers.getText());
		return conf;
	}

	public void saveConfig() {				
	    Map<ConfigTypes, String> conf=new HashMap<ConfigTypes,String>();						
	    conf.put(ConfigTypesCommon.CHECKER_PATH, codeCheckerDirectoryField.getText());
	    conf.put(ConfigTypesCommon.PYTHON_PATH, pythonEnvField.getText());
	    conf.put(ConfigTypesCommon.CHECKER_LIST, checkerListArg);
	    conf.put(ConfigTypesCommon.ANAL_THREADS, numThreads.getText());
	    conf.put(ConfigTypesCommon.COMPILERS, cLoggers.getText());

	    String g="true";
	    if (!useGlobalSettings)
	        g="false";			
	    conf.put(ConfigTypesProject.IS_GLOBAL, g);
	    Logger.log(IStatus.INFO, "Saving project settings: IS_GLOBAL:"+g);		
	    
	    if (globalGui){
	    	CcConfiguration.updateGlobalConfig(conf);
	    } else {
	    	config.updateProjectConfig(conf);
	    }				
	}
	
	   public void performDefaults() {
           loadConfig(true);
       }
	       
       public boolean isValid() {
           return true;
       }
       
       public void performOk() {
    	   Logger.log(IStatus.INFO, "Saving!");
           saveConfig();
	   }
	
	   public void init(IWorkbench workbench) {
           // TODO Auto-generated method stub
	   }

}
