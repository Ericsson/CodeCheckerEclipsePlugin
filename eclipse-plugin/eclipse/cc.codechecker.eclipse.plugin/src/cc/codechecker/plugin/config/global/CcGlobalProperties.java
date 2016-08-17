package cc.codechecker.plugin.config.global;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
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
import cc.codechecker.plugin.config.project.CcProjectProperties;
import cc.codechecker.plugin.itemselector.CheckerView;
import cc.codechecker.plugin.utils.CheckerItem;
import cc.codechecker.plugin.utils.CheckerItem.LAST_ACTION;

public class CcGlobalProperties extends PreferencePage implements IWorkbenchPreferencePage{

    //Logger
    private static final Logger logger = LogManager.getLogger(CcProjectProperties.class);

    IProject project;
    private static Text codeCheckerDirectoryField;
    private static Text pythonEnvField;
    private ArrayList<CheckerItem> checkersList = new ArrayList<>();
    private ArrayList<CheckerItem> defaultCheckersList = new ArrayList<>();
    private String checkercommand = "";
    private ScrolledForm form;

    @Override
    protected Control createContents(final Composite parent) {
        final FormToolkit toolkit = new FormToolkit(parent.getDisplay());

        form = toolkit.createScrolledForm(parent);
        form.getBody().setLayout(new GridLayout());

        Section packagepath = toolkit.createSection(form.getBody(),
                ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE
                | ExpandableComposite.EXPANDED);

        final Composite client = toolkit.createComposite(packagepath);
        client.setLayout(new GridLayout(3, true));
        packagepath.setClient(client);
        packagepath.setText("CodeChecker paths");

        Label codeCheckerDirectoryLabel = toolkit.createLabel(client, "CodeChecker package root directory");
        codeCheckerDirectoryLabel.setLayoutData(new GridData());
        codeCheckerDirectoryField = toolkit.createText(client, "");
        codeCheckerDirectoryField.setLayoutData(new GridData(GridData.FILL));
        codeCheckerDirectoryField.addListener(SWT.FocusOut, new Listener() {
            @Override
            public void handleEvent(Event event) {
                try {
                    testToCodeChecker();
                    form.setMessage("CodeChecker package directory is valid", 1);
                } catch (Exception e1) {
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
                if(dir != null) {
                    codeCheckerDirectoryField.setText(dir);
                    try {
                        testToCodeChecker();
                        form.setMessage("CodeChecker package directory is valid", 1);
                    } catch (Exception e1) {
                        form.setMessage("CodeChecker package directory is invalid", 3);
                    }
                }
            }
        });

        Label pythonEnvLabel = toolkit.createLabel(client, "Python virtualenv root directory (optional)");
        pythonEnvLabel.setLayoutData(new GridData());
        pythonEnvField = toolkit.createText(client, "");
        pythonEnvField.setLayoutData(new GridData(GridData.FILL));
        final Button pythonEnvFieldBrowse = new Button(client, SWT.PUSH);
        pythonEnvFieldBrowse.setText("Browse");
        pythonEnvFieldBrowse.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                DirectoryDialog dlg = new DirectoryDialog(client.getShell());
                dlg.setFilterPath(codeCheckerDirectoryField.getText());
                dlg.setText("Browse python environment");
                String dir = dlg.open();
                if(dir != null) {
                    pythonEnvField.setText(dir);
                }
            }
        });

        packagepath = toolkit.createSection(form.getBody(), ExpandableComposite.EXPANDED);
        final Composite client2 = toolkit.createComposite(packagepath);
        client2.setLayout(new GridLayout(3, true));
        packagepath.setClient(client2);
        final Button checkers = toolkit.createButton(client2, "Toggle enabled checkers", SWT.PUSH);
        checkers.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                Action action = new Action() {
                    @Override
                    public void run() {
                        Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

                        CheckerView dialog = new CheckerView(activeShell, checkersList);

                        int result = dialog.open();

                        if (result == 0) {
                            CcGlobalProperties.this.diffCheckersList(dialog.getCheckersList());
                        }
                    }
                };
                action.run();
            }
        });

        load();
        return form.getBody();
    }

    protected void diffCheckersList(List<CheckerItem> chl) {
        Collections.sort(defaultCheckersList);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < chl.size(); ++i) {
            if(!chl.get(i).equals(defaultCheckersList.get(i))) {
                sb.append(chl.get(i).getLastAction() == LAST_ACTION.SELECTION ? 
                        " -e " + chl.get(i).getText() + " " : 
                        " -d " + chl.get(i).getText() + " ");
            }
        }
        this.checkercommand = sb.toString();
        checkersList.clear();
        checkersList.addAll(chl);
    }

    private Optional<String> getPythonEnv() {
        String s = this.pythonEnvField.getText();
        if(s.isEmpty()) {
            return Optional.absent();
        } else {
            return Optional.of(s);
        }
    }

    private void testToCodeChecker() throws Exception {
        String codeCheckerDirectory = this.codeCheckerDirectoryField.getText();
        try {
            File dir = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()
                    + "/.codechecker/");
            if(!dir.exists()) {
                dir.mkdir();
            }
            CodeCheckEnvironmentChecker ccec = new CodeCheckEnvironmentChecker(getPythonEnv(),codeCheckerDirectory,dir.toString(), "");
            this.checkerList(ccec);
        } catch (Exception e) {
            throw e;
        }
    }

    private void checkerList(CodeCheckEnvironmentChecker ccec) {
        //Clear default list
        defaultCheckersList.clear();
        // new Checkers List
        String s = ccec.checkerList();
        String[] newCheckersSplit = s.split("\n");
        this.checkersList.clear();
        // old Checkers Command
        String[] checkersCommand = checkercommand.split(" ");
        List<String> oldCheckersCommand = Arrays.asList(checkersCommand);
        for(String it : newCheckersSplit) {
            CheckerItem defaultCheck = new CheckerItem(it.split(" ")[2]);
            CheckerItem check = new CheckerItem(it.split(" ")[2]);
            if(it.split(" ")[1].equals("+")) {
                defaultCheck.setLastAction(LAST_ACTION.SELECTION);
                this.defaultCheckersList.add(defaultCheck);
                if(oldCheckersCommand.contains(it.split(" ")[2]) && 
                        !oldCheckersCommand.contains(" -e "+ it.split(" ")[2])) {
                    check.setLastAction(LAST_ACTION.DESELECTION);
                } else {
                    check.setLastAction(LAST_ACTION.SELECTION);
                }
            } else {
                defaultCheck.setLastAction(LAST_ACTION.DESELECTION);
                this.defaultCheckersList.add(defaultCheck);
                if(oldCheckersCommand.contains(it.split(" ")[2]) && 
                        !oldCheckersCommand.contains(" -d "+ it.split(" ")[2])) {
                    check.setLastAction(LAST_ACTION.SELECTION);
                } else {
                    check.setLastAction(LAST_ACTION.DESELECTION);
                }
            }
            checkersList.add(check);
        }
    }

    public void load() {
        codeCheckerDirectoryField.setText(CcConfiguration.getGlobalCodecheckerDirectory());
        pythonEnvField.setText(CcConfiguration.getGlobalPythonEnv().or(""));
        checkercommand = CcConfiguration.getGlobalCheckerCommand();
        try {
            testToCodeChecker();
            form.setMessage("CodeChecker package directory is valid", 1);
        } catch (Exception e1) {
            form.setMessage("CodeChecker package directory is invalid", 3);
        }
    }

    public void save() {
        CcConfiguration.updateGlobal(codeCheckerDirectoryField.getText(), pythonEnvField.getText(), checkercommand);
    }

    @Override
    public void performDefaults() {
        this.checkersList.clear();
        this.checkersList.addAll(defaultCheckersList);
        form.setText("Enabled checkers have been reset to defaults");
        super.performDefaults();;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean performOk() {
        logger.log(Level.INFO, "SERVER_GUI_MSG >> Saving!");
        save();
        return super.performOk();
    }

    @Override
    public void init(IWorkbench workbench) {
        // TODO Auto-generated method stub

    }
}
