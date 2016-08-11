package cc.codechecker.plugin.config.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import com.google.common.base.Optional;

import cc.codechecker.api.runtime.CodeCheckEnvironmentChecker;
import cc.codechecker.plugin.config.CcConfiguration;
import cc.codechecker.plugin.config.global.CcGlobalProperties;
import cc.codechecker.plugin.itemselector.CheckerView;
import cc.codechecker.plugin.utils.CheckerItem;
import cc.codechecker.plugin.utils.CheckerItem.LAST_ACTION;

import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

public class CcProjectProperties extends PropertyPage implements IWorkbenchPropertyPage {

    //Logger
    private static final Logger logger = LogManager.getLogger(CcProjectProperties.class);

    IProject project;
    private Text codeCheckerDirectoryField;
    private Text pythonEnvField;
    private ArrayList<CheckerItem> checkersList = new ArrayList<>();
    private ArrayList<CheckerItem> defaultCheckersList = new ArrayList<>();
    private String checkercommand = "";

    @Override
    protected Control createContents(Composite parent) {
        final FormToolkit toolkit = new FormToolkit(parent.getDisplay());

        final ScrolledForm form = toolkit.createScrolledForm(parent);
        form.setMessage("Not CodeChecker Check!", 1);
        form.getBody().setLayout(new GridLayout());

        Section section = toolkit.createSection(form.getBody(),
                ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE
                | ExpandableComposite.EXPANDED);

        final Composite client = toolkit.createComposite(section);
        client.setLayout(new GridLayout(3, true));
        section.setClient(client);
        section.setText("CodeChecker Configuration");
        Label codeCheckerDirectoryLabel = toolkit.createLabel(client, "CodeChecker package root directory");
        codeCheckerDirectoryLabel.setLayoutData(new GridData());
        codeCheckerDirectoryField = toolkit.createText(client, "");
        codeCheckerDirectoryField.setLayoutData(new GridData(GridData.FILL));
        codeCheckerDirectoryField.addListener(SWT.FocusOut, new Listener() {
            @Override
            public void handleEvent(Event event) {
                try {
                    testToCodeChecker();
                    form.setMessage("CodeChecker package directory is valid!", 1);
                } catch (Exception e1) {
                    form.setMessage("CodeChecker package directory is invalid!", 3);
                }
            }
        });

        final Button codeCheckerDirectoryFieldBrowse = new Button(client, SWT.PUSH);
        codeCheckerDirectoryFieldBrowse.setText("Browse");
        codeCheckerDirectoryFieldBrowse.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                DirectoryDialog dlg = new DirectoryDialog(client.getShell());
                dlg.setFilterPath(codeCheckerDirectoryField.getText());
                dlg.setText("SWT's DirectoryDialog");
                dlg.setMessage("Select a directory");
                String dir = dlg.open();
                if(dir != null) {
                    codeCheckerDirectoryField.setText(dir);
                    try {
                        testToCodeChecker();
                        form.setMessage("CodeChecker package directory is valid!", 1);
                    } catch (Exception e1) {
                        form.setMessage("CodeChecker package directory is invalid!", 3);
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
                dlg.setText("SWT's DirectoryDialog");
                dlg.setMessage("Select a directory");
                String dir = dlg.open();
                if(dir != null) {
                    pythonEnvField.setText(dir);
                }
            }
        });

        final Button checkers = toolkit.createButton(client, "Checker configuration!", SWT.PUSH);
        checkers.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                Action action = new Action() {
                    @Override
                    public void run() {
                        Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

                        CheckerView dialog = new CheckerView(activeShell, checkersList);

                        int result = dialog.open();

                        if (result == 0) {
                            CcProjectProperties.this.diffCheckersList(dialog.getCheckersList());
                        }
                    }
                };
                action.run();
            }
        });

        load(form);
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

    private CcConfiguration getConfiguration() {
        project = (IProject) this.getElement().getAdapter(IProject.class);
        return new CcConfiguration(project);
    }

    public void load(ScrolledForm form) {
        CcConfiguration ccc = getConfiguration();
        codeCheckerDirectoryField.setText(ccc.getCodecheckerDirectory());
        pythonEnvField.setText(ccc.getPythonEnv().or(""));
        checkercommand = ccc.getCheckerCommand();
        try {
            testToCodeChecker();
            form.setMessage("CodeChecker package directory is valid!", 1);
        } catch (Exception e1) {
            form.setMessage("CodeChecker package directory is invalid!", 3);
        }
    }

    public void save() {
        CcConfiguration ccc = getConfiguration();
        System.out.println(checkercommand);
        ccc.update(codeCheckerDirectoryField.getText(), pythonEnvField.getText(), checkercommand);
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
}
