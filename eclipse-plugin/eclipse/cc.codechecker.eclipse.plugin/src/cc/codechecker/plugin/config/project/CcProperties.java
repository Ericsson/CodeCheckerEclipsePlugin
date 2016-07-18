package cc.codechecker.plugin.config.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

public class CcProperties extends PropertyPage implements IWorkbenchPropertyPage {

	//Logger
	private static final Logger logger = LogManager.getLogger(CcProperties.class);
	
    IProject project;
    private Text codeCheckerDirectoryField;
    private Text pythonEnvField;

    @Override
    protected Control createContents(Composite parent) {

        parent.setLayout(new GridLayout(1, false));
        FormToolkit toolkit = new FormToolkit(parent.getDisplay());

        Form form = toolkit.createForm(parent);

        GridLayout mylayout = new GridLayout();
        mylayout.numColumns = 2;
        form.getBody().setLayout(mylayout);

        Label codeCheckerDirectoryLabel = toolkit.createLabel(form.getBody(), "CodeChecker package root directory");
        codeCheckerDirectoryLabel.setLayoutData(new GridData());
        codeCheckerDirectoryField = toolkit.createText(form.getBody(), "");
        codeCheckerDirectoryField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label pythonEnvLabel = toolkit.createLabel(form.getBody(), "Python virtualenv root directory (optional)");
        pythonEnvLabel.setLayoutData(new GridData());
        pythonEnvField = toolkit.createText(form.getBody(), "");
        pythonEnvField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        load();

        return form.getBody();
    }

    private CcConfiguration getConfiguration() {
        project = (IProject) this.getElement().getAdapter(IProject.class);
        return new CcConfiguration(project);
    }

    public void load() {
        CcConfiguration ccc = getConfiguration();

        codeCheckerDirectoryField.setText(ccc.getCodecheckerDirectory());
        pythonEnvField.setText(ccc.getPythonEnv().or(""));
    }

    public void save() {
        CcConfiguration ccc = getConfiguration();

        ccc.update(codeCheckerDirectoryField.getText(), pythonEnvField.getText());
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
