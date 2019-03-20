package cc.codechecker.plugin.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IMessageProvider;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import cc.codechecker.plugin.Logger;
import cc.codechecker.plugin.config.Config.ConfigTypes;
import cc.codechecker.plugin.itemselector.CheckerView;
import cc.codechecker.plugin.runtime.CodeCheckEnvironmentChecker;
import cc.codechecker.plugin.utils.CheckerItem;
import cc.codechecker.plugin.utils.CheckerItem.LAST_ACTION;

/**
 * Global and project level preferences pages.
 *
 */
public class CommonGui {

    private static final String CC_BINARY = "/bin/CodeChecker";
    private static final String VALID_PACKAGE = "CodeChecker package directory is valid";
    private static final String INVALID_PACKAGE = "CodeChecker package directory is invalid";
    private static final String BROSWE = "Browse";
    private static final String CHECKER_ENABLED = " -e ";
    private static final String CHECKER_DISABLED = " -d ";
    private static final String CHECKER_SEPARATOR = " ";

    private static final int TEXTWIDTH = 200;
    private static final int FORM_COLUMNS = 3;

    private boolean globalGui;// whether this class is for global or project
                              // specific preferences
    private boolean useGlobalSettings;// if this is project specific page,
                                      // whether to use global preferences
    private CcConfiguration config;
    private Text codeCheckerDirectoryField;// codechecker dir
    private Text pythonEnvField;// CodeChecker python env
    private Text numThreads;// #of analysis threads
    private Text cLoggers;// #C compiler commands to catch

    private String checkerListArg = "";
    private ScrolledForm form;

    private Button globalcc;
    private Button projectcc;
    
    /**
     * Constructor to be used, when only global preferences are to be set.
     */
    public CommonGui() {
        globalGui = true;
    }
	
	/**
	 * Constructor for setting project related preferences.
	 * @param proj The project which preferences to be set
	 */
    public CommonGui(IProject proj) {
        config = CodeCheckerContext.getInstance().getConfigForProject(proj);
        useGlobalSettings = config.isGlobal();
        globalGui = false;
    }

    /**
     * Adds a {@link Text} input field with a {@link Label}.
     * @param toolkit This toolkit is the factory that makes the Controls.
     * @param comp The parent {@link Composite} that the new {@link Control} is to be added.
     * @param labelText The text that will be added to the {@link Label}.
     * @param def The default texdt thats displayed on the {@link Text}.
     * @return The newly created textfield.
     */
    protected Text addTextField(FormToolkit toolkit, Composite comp, String labelText, String def) {
        Text ret;
        Label label = toolkit.createLabel(comp, labelText);
        label.setLayoutData(new GridData());
        ret = toolkit.createText(comp, def, SWT.MULTI | SWT.WRAP | SWT.BORDER);
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
        gd.widthHint = TEXTWIDTH;
        ret.setLayoutData(gd);
        return ret;
    }

    /**
     * The actual creation of the controls happens in this method.
     * Creates the {@link ScrolledForm} with a {@link FormToolkit}. This form is used as a canvas to add the input
     * configuration fields for the global or project level configs. Also a global/project selector is added here
     * when the class is constructed with a project.
     * @param parent The parent which the form to be created on.
     * @return The form itself.
     */
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
        layout.maxNumColumns = 1;
        form.getBody().setLayout(layout);

        Section globalConfigSection = null;
        if (!globalGui) {
            globalConfigSection = toolkit.createSection(form.getBody(), ExpandableComposite.EXPANDED);
        }
        final Section checkerConfigSection = toolkit.createSection(form.getBody(),
                ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
        checkerConfigSection.setEnabled(true);

        final Composite client = toolkit.createComposite(checkerConfigSection);
        client.setLayout(new GridLayout(FORM_COLUMNS, false));

        checkerConfigSection.setClient(client);
        checkerConfigSection.setText("Configuration");

        codeCheckerDirectoryField = addTextField(toolkit, client, "CodeChecker package root directory", "");
        codeCheckerDirectoryField.addListener(SWT.FocusOut, new Listener() {
            @Override
            public void handleEvent(Event event) {
                try {
                    Map<ConfigTypes, String> changedConfig = getConfigFromFields();
                    CodeCheckEnvironmentChecker.getCheckerEnvironment(changedConfig,
                            changedConfig.get(ConfigTypes.CHECKER_PATH) + CC_BINARY);
                    form.setMessage(VALID_PACKAGE, IMessageProvider.INFORMATION);
                } catch (IllegalArgumentException e1) {
                    form.setMessage(INVALID_PACKAGE, IMessageProvider.ERROR);
                }
            }
        });

        final Button codeCheckerDirectoryFieldBrowse = new Button(client, SWT.PUSH);
        codeCheckerDirectoryFieldBrowse.setText(BROSWE);
        codeCheckerDirectoryFieldBrowse.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                DirectoryDialog dlg = new DirectoryDialog(client.getShell());
                dlg.setFilterPath(codeCheckerDirectoryField.getText());
                dlg.setText("Browse codechecker root");
                String dir = dlg.open();
                if (dir != null) {
                    codeCheckerDirectoryField.setText(dir);
                    try {
                        Map<ConfigTypes, String> changedConfig = getConfigFromFields();
                        CodeCheckEnvironmentChecker.getCheckerEnvironment(changedConfig,
                                changedConfig.get(ConfigTypes.CHECKER_PATH) + CC_BINARY);
                        form.setMessage(VALID_PACKAGE, IMessageProvider.INFORMATION);
                    } catch (IllegalArgumentException e1) {
                        form.setMessage(INVALID_PACKAGE, IMessageProvider.ERROR);
                    }
                }
            }
        });

        pythonEnvField = addTextField(toolkit, client, "Python virtualenv root directory (optional)", "");
        final Button pythonEnvFieldBrowse = new Button(client, SWT.PUSH);
        pythonEnvFieldBrowse.setText(BROSWE);
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
        cLoggers = addTextField(toolkit, client, "Compiler commands to log", "gcc:g++:clang:clang++");
        toolkit.createLabel(client, "");

        Map<ConfigTypes, String> configMap = loadConfig(false);
        try {
            CodeCheckEnvironmentChecker.getCheckerEnvironment(configMap,
                    configMap.get(ConfigTypes.CHECKER_PATH) + CC_BINARY);
            form.setMessage(VALID_PACKAGE, IMessageProvider.INFORMATION);
        } catch (IllegalArgumentException e1) {
            form.setMessage(INVALID_PACKAGE, IMessageProvider.ERROR);
        }

        final Button checkers = toolkit.createButton(client, "Toggle enabled checkers", SWT.PUSH);
        checkers.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                Action action = new Action() {
                    @Override
                    public void run() {
                        Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                        Map<ConfigTypes, String> config = getConfigFromFields();
                        try {
                            CodeCheckEnvironmentChecker checkerEnv = new CodeCheckEnvironmentChecker(config);
                            ArrayList<CheckerItem> checkersList = getCheckerList(checkerEnv);
                            CheckerView dialog = new CheckerView(activeShell, checkersList);

                            int result = dialog.open();

                            if (result == 0) {
                                checkerListArg = checkerListToCheckerListArg(dialog.getCheckersList());
                            }
                        } catch (IllegalArgumentException e) {
                            Logger.log(IStatus.INFO, "Codechecker environment is invalid" + e);
                        }
                    }
                };
                action.run();
            }
        });
        if (!globalGui) {
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
                    useGlobalSettings = true;
                    setFields(config.getProjectConfig(useGlobalSettings));
                }
            });
            projectcc = toolkit.createButton(client3, "Use project configuration", SWT.RADIO);
            projectcc.setSelection(!useGlobalSettings);
            projectcc.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent event) {
                    recursiveSetEnabled(checkerConfigSection, true);
                    useGlobalSettings = false;
                    setFields(config.getProjectConfig(useGlobalSettings));
                }
            });

        }
        return form.getBody();
    }

	/**
	 * Recursive control state modifier.
	 * If the control is {@link Composite} toggles it state and all of it's children {@link Control}.
	 * @param control The parent control.
	 * @param b The state to be set.
	 */
    public void recursiveSetEnabled(Control control, Boolean b) {
        if (control instanceof Composite) {
            Composite comp = (Composite) control;
            for (Control c : comp.getChildren())
                recursiveSetEnabled(c, b);
        } else {
            control.setEnabled(b);
        }
    }

    /**
     * Returns The all checkers from CodeChecker.
     * @param ccec The Environment Checker to be used.
     * @return A list of all available checkers.
     */
    private ArrayList<CheckerItem> getCheckerList(CodeCheckEnvironmentChecker ccec) {
        // ArrayList<CheckerItem> defaultCheckersList = new ArrayList<>();
        ArrayList<CheckerItem> checkersList = new ArrayList<>(); //
        // new Checkers List
        String s = ccec.getCheckerList();
        String[] newCheckersSplit = s.split("\n");
        // old Checkers Command
        //String[] checkersCommand = checkerListArg.split(CHECKER_SEPARATOR);
        //List<String> oldCheckersCommand = Arrays.asList(checkersCommand);
        for (String it : newCheckersSplit) {
            // String checkerName = it.split(" ")[2];
            String checkerName = it;
            CheckerItem check = new CheckerItem(checkerName);
            boolean defaultEnabled = false;

            if (it.split(CHECKER_SEPARATOR)[1].equals("+"))
                defaultEnabled = true;
            if (defaultEnabled) {
                if (checkerListArg.contains(CHECKER_DISABLED + checkerName)) {
                    check.setLastAction(LAST_ACTION.DESELECTION);
                } else {
                    check.setLastAction(LAST_ACTION.SELECTION);
                }
            } else {
                if (checkerListArg.contains(CHECKER_ENABLED + checkerName)) {
                    check.setLastAction(LAST_ACTION.SELECTION);
                } else {
                    check.setLastAction(LAST_ACTION.DESELECTION);
                }
            }
            checkersList.add(check);
        }
        return checkersList;
    }

    /**
     * Returns Stringified checkerlist configuration.
     * @param chl The internal list of checkers.
     * @return The string list of checkers prefixed with state.
     */
    protected String checkerListToCheckerListArg(List<CheckerItem> chl) {
        StringBuilder checkerListArg = new StringBuilder();
        for (int i = 0; i < chl.size(); ++i) {
            if (chl.get(i).getLastAction() == LAST_ACTION.SELECTION)
                checkerListArg.append(CHECKER_ENABLED + chl.get(i).getText() + CHECKER_SEPARATOR);
            else
                checkerListArg.append(CHECKER_DISABLED + chl.get(i).getText() + CHECKER_SEPARATOR);
        }
        return checkerListArg.toString();
    }

    /**
     * Loads config from {@link CcConfiguration}.
     * @param resetToDefault If set, the default config is used.
     * @return The configuration map thats represent the preferences.
     */
    public Map<ConfigTypes, String> loadConfig(boolean resetToDefault) {
        Map<ConfigTypes, String> ret = null;
        if (!resetToDefault) {
            if (globalGui)
                ret = CcConfiguration.getGlobalConfig();
            else {
                ret = config.getProjectConfig(null);
            }
        } else
            ret = config.getDefaultConfig();

        setFields(ret);
        return ret;
    }
	
	/**
	 * Sets the form fields with the given config maps values.
	 * @param config The config which values are taken.
	 */
    public void setFields(Map<ConfigTypes, String> config) {
        codeCheckerDirectoryField.setText(config.get(ConfigTypes.CHECKER_PATH));
        pythonEnvField.setText(config.get(ConfigTypes.PYTHON_PATH));
        checkerListArg = config.get(ConfigTypes.CHECKER_LIST);
        cLoggers.setText(config.get(ConfigTypes.COMPILERS));
        numThreads.setText(config.get(ConfigTypes.ANAL_THREADS));
    }
	
    /**
     * Returns a config map form the inputfields.
     * @return The config map.
     */
    public Map<ConfigTypes, String> getConfigFromFields() {
        Map<ConfigTypes, String> conf = new HashMap<>();
        conf.put(ConfigTypes.CHECKER_PATH, codeCheckerDirectoryField.getText());
        conf.put(ConfigTypes.PYTHON_PATH, pythonEnvField.getText());
        conf.put(ConfigTypes.CHECKER_LIST, checkerListArg);
        conf.put(ConfigTypes.ANAL_THREADS, numThreads.getText());
        conf.put(ConfigTypes.COMPILERS, cLoggers.getText());
        return conf;
    }

    /**
     * Updates persistent configuration through the {@link CcConfiguration}.
     */
    public void saveConfig() {
        Map<ConfigTypes, String> conf = new HashMap<ConfigTypes, String>();
        conf.put(ConfigTypes.CHECKER_PATH, codeCheckerDirectoryField.getText());
        conf.put(ConfigTypes.PYTHON_PATH, pythonEnvField.getText());
        conf.put(ConfigTypes.CHECKER_LIST, checkerListArg);
        conf.put(ConfigTypes.ANAL_THREADS, numThreads.getText());
        conf.put(ConfigTypes.COMPILERS, cLoggers.getText());

        String g = "true";
        if (!useGlobalSettings)
            g = "false";
        conf.put(ConfigTypes.IS_GLOBAL, g);
        Logger.log(IStatus.INFO, "Saving project settings: IS_GLOBAL:" + g);

        if (globalGui) {
            CcConfiguration.updateGlobalConfig(conf);
        } else {
            config.updateProjectConfig(conf);
        }
    }
	
    /**
     * Used by preferences page.
     */
    public void performDefaults() {
        loadConfig(true);
    }

    /**
     * Used by preferences page.
     * @return Always true.
     */
    public boolean isValid() {
        return true;
    }

    /**
     * Used by preferences page.
     */
    public void performOk() {
        Logger.log(IStatus.INFO, "Saving!");
        saveConfig();
    }

    /**
     * Used by preferences page.
     * @param workbench The workbench thats used.
     */
    public void init(IWorkbench workbench) {
        // TODO Auto-generated method stub
    }

}
