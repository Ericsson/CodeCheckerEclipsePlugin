package org.codechecker.eclipse.plugin.config;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codechecker.eclipse.plugin.Logger;
import org.codechecker.eclipse.plugin.codechecker.CodeCheckerFactory;
import org.codechecker.eclipse.plugin.codechecker.ICodeChecker;
import org.codechecker.eclipse.plugin.codechecker.locator.CodeCheckerLocatorService;
import org.codechecker.eclipse.plugin.codechecker.locator.EnvCodeCheckerLocatorService;
import org.codechecker.eclipse.plugin.codechecker.locator.InvalidCodeCheckerException;
import org.codechecker.eclipse.plugin.codechecker.locator.PreBuiltCodeCheckerLocatorService;
import org.codechecker.eclipse.plugin.codechecker.locator.ResolutionMethodTypes;
import org.codechecker.eclipse.plugin.config.Config.ConfigTypes;
import org.codechecker.eclipse.plugin.config.global.CcGlobalConfiguration;
import org.codechecker.eclipse.plugin.config.project.CodeCheckerProject;
import org.codechecker.eclipse.plugin.itemselector.CheckerView;
import org.codechecker.eclipse.plugin.runtime.ShellExecutorHelperFactory;
import org.codechecker.eclipse.plugin.utils.CheckerItem;
import org.codechecker.eclipse.plugin.utils.CheckerItem.LAST_ACTION;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Global and project level preferences pages.
 *
 */
public class CommonGui {

    public static final String CC_BIN_LABEL = "CodeChecker binary:";

    private static final String VALID_PACKAGE = "CodeChecker being used: ";
    private static final String BROSWE = "Browse";
    private static final String CHECKER_ENABLED = " -e ";
    private static final String CHECKER_DISABLED = " -d ";
    private static final String CHECKER_SEPARATOR = " ";

    private static final int TEXTWIDTH = 200;
    private static final int FORM_COLUMNS = 3;
    private static final int FORM_ONE_ROW = 1;

    private boolean globalGui;// whether this class is for global or project
                              // specific preferences
    private boolean useGlobalSettings;// if this is project specific page,
                                      // whether to use global preferences
    private CcConfigurationBase config;
    private CodeCheckerProject cCProject;
    private ICodeChecker codeChecker;

    private Button pathCc;
    private Button preBuiltCc;

    private Composite ccDirClient;
    private Text codeCheckerDirectoryField;// codechecker dir

    private ResolutionMethodTypes currentResMethod;

    private Section checkerConfigSection;
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
        config = CcGlobalConfiguration.getInstance();
        globalGui = true;
    }
	
	/**
	 * Constructor for setting project related preferences.
	 * @param proj The project which preferences to be set
	 */
    public CommonGui(IProject proj) {
        cCProject = CodeCheckerContext.getInstance().getCcProject(proj);
        config = cCProject.getCurrentConfig();
        useGlobalSettings = cCProject.isGlobal();
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

        loadConfig(false);

        Section globalConfigSection = null;
        if (!globalGui) {
            globalConfigSection = toolkit.createSection(form.getBody(), ExpandableComposite.EXPANDED);
        }

        Section packageSection = createConfigSection(toolkit);

        checkerConfigSection = toolkit.createSection(form.getBody(),
                ExpandableComposite.SHORT_TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
        checkerConfigSection.setEnabled(true);

        final Composite comp = toolkit.createComposite(checkerConfigSection);
        comp.setLayout(new GridLayout(FORM_COLUMNS, false));

        checkerConfigSection.setClient(comp);
        checkerConfigSection.setText("Analysis options");

        numThreads = addTextField(toolkit, comp, "Number of analysis threads", "4");
        toolkit.createLabel(comp, "");
        cLoggers = addTextField(toolkit, comp, "Compiler commands to log", "gcc:g++:clang:clang++");
        toolkit.createLabel(comp, "");

        final Button checkers = toolkit.createButton(comp, "Toggle enabled checkers", SWT.PUSH);
        checkers.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                Action action = new Action() {
                    @Override
                    public void run() {
                        Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                        //Map<ConfigTypes, String> config = getConfigFromFields();
                        try {
                            ArrayList<CheckerItem> checkersList = getCheckerList();
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
        checkers.setData("org.eclipse.swtbot.widget.checkersKey", "checkesButton");
        if (!globalGui) {
            recursiveSetEnabled(form.getBody(), !useGlobalSettings);
            final Composite client3 = toolkit.createComposite(globalConfigSection);
            client3.setLayout(new GridLayout(2, true));
            globalConfigSection.setClient(client3);
            globalcc = toolkit.createButton(client3, "Use global configuration", SWT.RADIO);
            globalcc.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent event) {
                    if (globalcc.getSelection()) {
                        recursiveSetEnabled(checkerConfigSection, false);
                        recursiveSetEnabled(packageSection, false);
                        useGlobalSettings = true;
                        config = cCProject.getGlobal();
                        setFields();
                        locateCodeChecker();
                    }
                }
            });
            globalcc.setSelection(useGlobalSettings);
            projectcc = toolkit.createButton(client3, "Use project configuration", SWT.RADIO);
            projectcc.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent event) {
                    if (projectcc.getSelection()) {
                        recursiveSetEnabled(checkerConfigSection, true);
                        recursiveSetEnabled(packageSection, true);
                        useGlobalSettings = false;
                        config = cCProject.getLocal();
                        setFields();
                        changeDirectoryInputs();
                        locateCodeChecker();
                    }
                }
            });
            projectcc.setSelection(!useGlobalSettings);
            changeDirectoryInputs();
        }
        setFields();
        locateCodeChecker();
        return form.getBody();
    }

    /**
     * Creates the resolution method group, and the package directory inputs.
     * 
     * @param toolkit
     *            The toolkit to be used.
     * @return The encapsulating Section.
     */
    private Section createConfigSection(FormToolkit toolkit) {

        final Section packageConfigSection = toolkit.createSection(form.getBody(),
                ExpandableComposite.SHORT_TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
        packageConfigSection.setEnabled(true);

        final Composite client = toolkit.createComposite(packageConfigSection);
        client.setLayout(new GridLayout(FORM_COLUMNS, false));

        packageConfigSection.setClient(client);
        packageConfigSection.setText("Configuration");

        Group resolutionType = new Group(client, SWT.NULL);
        resolutionType.setText("CodeChecker resolution method.");
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(resolutionType);
        GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER)
                .span(FORM_COLUMNS, FORM_ONE_ROW).applyTo(resolutionType);
        resolutionType.setBackground(client.getBackground());

        ccDirClient = toolkit.createComposite(client);
        GridLayoutFactory.fillDefaults().numColumns(FORM_COLUMNS).applyTo(ccDirClient);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(FORM_COLUMNS, FORM_ONE_ROW)
                .applyTo(ccDirClient);
        ccDirClient.setBackground(client.getBackground());

        pathCc = toolkit.createButton(resolutionType, "Search in PATH", SWT.RADIO);
        pathCc.setData(ResolutionMethodTypes.PATH);
        pathCc.addSelectionListener(new PackageResolutionSelectionAdapter());

        preBuiltCc = toolkit.createButton(resolutionType, "Pre built package", SWT.RADIO);
        preBuiltCc.setData(ResolutionMethodTypes.PRE);
        preBuiltCc.addSelectionListener(new PackageResolutionSelectionAdapter());

        codeCheckerDirectoryField = addTextField(toolkit, ccDirClient, CC_BIN_LABEL, "");
        codeCheckerDirectoryField.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                locateCodeChecker();
            }
        });

        Button codeCheckerDirectoryFieldBrowse = new Button(ccDirClient, SWT.PUSH);
        codeCheckerDirectoryFieldBrowse.setText(BROSWE);
        codeCheckerDirectoryFieldBrowse.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                FileDialog dlg = new FileDialog(client.getShell());
                dlg.setFilterPath(codeCheckerDirectoryField.getText());
                dlg.setText("Browse CodeChecker binary");
                String dir = dlg.open();
                if (dir != null) {
                    codeCheckerDirectoryField.setText(dir);
                    locateCodeChecker();
                }
            }
        });

        changeDirectoryInputs();
        return packageConfigSection;

    }

    /**
     * Changes directory input widgets depending on the resolution method radio
     * group.
     */
    public void changeDirectoryInputs() {
        if (!useGlobalSettings)
            switch (currentResMethod) {
                case PATH:
                    recursiveSetEnabled(ccDirClient, false);
                    break;
                case PRE:
                    recursiveSetEnabled(ccDirClient, true);
                    break;
                default:
                    break;
            }
    }

    /**
     * Tries to find a CodeChecker package.
     */
    public void locateCodeChecker() {
        CodeCheckerLocatorService serv = null;
        switch (currentResMethod) {
            case PATH:
                serv = new EnvCodeCheckerLocatorService();
                break;
            case PRE:
                serv = new PreBuiltCodeCheckerLocatorService();
                break;
            default:
                break;
        }
        ICodeChecker cc = null;
        try {
            cc = serv.findCodeChecker(Paths.get(codeCheckerDirectoryField.getText()),
                    new CodeCheckerFactory(), new ShellExecutorHelperFactory());
            form.setMessage(VALID_PACKAGE + cc.getLocation().toString(), IMessageProvider.INFORMATION);
            if (globalGui || (!globalGui && !useGlobalSettings))
                recursiveSetEnabled(checkerConfigSection, true);
        } catch (InvalidCodeCheckerException | IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            recursiveSetEnabled(checkerConfigSection, false);
            form.setMessage(e.getMessage(), IMessageProvider.ERROR);
        }
        this.codeChecker = cc;
    }

    /**
     * Recursive control state modifier. If the control is {@link Composite} toggles
     * it state and all of it's children {@link Control}.
     * 
     * @param control
     *            The parent control.
     * @param b
     *            The state to be set.
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
     * @return A list of all available checkers.
     */
    private ArrayList<CheckerItem> getCheckerList() {
        // ArrayList<CheckerItem> defaultCheckersList = new ArrayList<>();
        ArrayList<CheckerItem> checkersList = new ArrayList<>(); //
        // new Checkers List
        String s = codeChecker.getCheckers();
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
            ret = config.get();
        } else
            ret = config.getDefaultConfig();
        currentResMethod = ResolutionMethodTypes.valueOf(config.get(ConfigTypes.RES_METHOD));
        return ret;
    }
	
	/**
	 * Sets the form fields with the given config maps values.
	 */
    public void setFields() {
        pathCc.setSelection(false);
        preBuiltCc.setSelection(false);
        switch (currentResMethod) {
            case PATH:
                pathCc.setSelection(true);
                break;
            case PRE:
                preBuiltCc.setSelection(true);
                break;
            default:
                break;
        }
        codeCheckerDirectoryField.setText(config.get(ConfigTypes.CHECKER_PATH));
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
        conf.put(ConfigTypes.CHECKER_LIST, checkerListArg);
        conf.put(ConfigTypes.ANAL_THREADS, numThreads.getText());
        conf.put(ConfigTypes.COMPILERS, cLoggers.getText());
        conf.put(ConfigTypes.RES_METHOD, currentResMethod.toString());
        config.setCodeChecker(codeChecker);
        config.update(conf);
        if(!globalGui)
            cCProject.useGlobal(useGlobalSettings);
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

    /**
     * Callback for the Resolution method selection listener.
     */
    private class PackageResolutionSelectionAdapter extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent event) {
            boolean isSelected = ((Button) event.getSource()).getSelection();
            if (isSelected) {
                currentResMethod = (ResolutionMethodTypes) event.widget.getData();
                changeDirectoryInputs();
                locateCodeChecker();
            }
        }

    }
}
