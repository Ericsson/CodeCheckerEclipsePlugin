package org.codechecker.eclipse.plugin.init;

import org.codechecker.eclipse.plugin.Logger;
import org.codechecker.eclipse.plugin.config.CodeCheckerContext;
import org.codechecker.eclipse.plugin.report.job.AnalyzeJob;
import org.codechecker.eclipse.plugin.report.job.JobDoneChangeListener;
import org.codechecker.eclipse.plugin.report.job.PlistParseJob;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

public class EditorPartListener implements IPartListener {

    @Override
    public void partActivated(IWorkbenchPart partRef) {
        if (!(partRef instanceof IEditorPart)) {
            return;
        }
        Logger.log(IStatus.INFO, "Editor changed : " + partRef.getClass().getName());
        CodeCheckerContext.getInstance().refreshChangeEditorPart((IEditorPart) partRef);
    }

    @Override
    public void partBroughtToTop(IWorkbenchPart part) {
    }

    @Override
    public void partClosed(IWorkbenchPart part) {
    }

    @Override
    public void partDeactivated(IWorkbenchPart part) {
    }

    @Override
    public void partOpened(IWorkbenchPart part) {
        Logger.log(IStatus.INFO, "Editor Opened : " + part.getClass().getName());
        // if an editor is opened parse the reports if there is any.
        IFile file;
        try {
            file = ((IFileEditorInput) ((IEditorPart) part).getEditorInput()).getFile();
        } catch (ClassCastException e) {
            Logger.log(IStatus.WARNING, "Not a File Editor,early returning");
            return;
        }

        IProject project = file.getProject();
        if (CodeCheckerContext.getInstance().getCcProject(project) == null)
            return;

        AnalyzeJob analyzeJob = new AnalyzeJob(project, file.getLocation().toFile().toPath());
        PlistParseJob plistParseJob = new PlistParseJob(project);
        analyzeJob.setRule(project);
        plistParseJob.setRule(project);
        plistParseJob.addJobChangeListener(new JobDoneChangeListener() {
            @Override
            public void done(IJobChangeEvent event) {
                CodeCheckerContext.getInstance().refresAsync(project);
            }
        });
        analyzeJob.schedule();
        plistParseJob.schedule();
    }

}
