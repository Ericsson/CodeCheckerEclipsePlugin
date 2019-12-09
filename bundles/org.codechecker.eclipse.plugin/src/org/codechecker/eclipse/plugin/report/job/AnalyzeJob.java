package org.codechecker.eclipse.plugin.report.job;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.codechecker.eclipse.plugin.Logger;
import org.codechecker.eclipse.plugin.config.CcConfigurationBase;
import org.codechecker.eclipse.plugin.config.CodeCheckerContext;
import org.codechecker.eclipse.plugin.config.project.CodeCheckerProject;
import org.codechecker.eclipse.plugin.views.report.list.ReportListView;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.PlatformUI;

/**
 * This class represents a CodeChecker analyze command.
 *
 */
public class AnalyzeJob extends Job {

    private CodeCheckerProject project;
    private CcConfigurationBase config;
    private Path logFile;
    private Path fileToAnalyze;
    private ReportListView projectView;

    private Path skipFile;

    /**
     * Hidden due analyze is meaningless without a project.
     * @param name unused.
     */
    private AnalyzeJob(String name) {
        super(name);
    }

    /**
     * Constructor for making an analyze job.
     * @param project The eclipse project that's will be analyzed.
     */
    public AnalyzeJob(IProject project) {
        this(project, null);
    }

    /**
     * Constructor for making an analyze job, for one file;
     * 
     * @param project
     *            The eclipse project that's will be analyzed.
     */
    public AnalyzeJob(IProject project, Path file) {
        super("Running CodeChecker Analyze");
        this.fileToAnalyze = file;
        this.project = CodeCheckerContext.getInstance().getCcProject(project);
        config = this.project.getCurrentConfig();

        projectView = (ReportListView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getActivePage().findView(ReportListView.ID);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        Logger.log(IStatus.INFO, "Running AnalyzeJob");

        if (projectView != null) {
            projectView.refresAsync(projectView::setAnalyzeMsg);
            projectView.refresAsync(projectView::clearModel);
        }

        logFile = project.getLogFileLocation();

        AtomicInteger taskCount = new AtomicInteger(0);
        if (fileToAnalyze != null) {
            createSkipFile();
            taskCount.incrementAndGet();
        } else {
            // read the file
            try {
                Files.lines(logFile).forEach(new Consumer<String>() {

                    @Override
                    public void accept(String t) {
                        if (t.contains("\"command\":")) {
                            taskCount.incrementAndGet();
                        }
                    }
                });
            } catch (IOException e) {
                Logger.log(IStatus.ERROR, "Couldn't read logFile!");
                if (projectView != null)
                    projectView.refresAsync(projectView::setEmptyMsg);
            }
        }

        monitor.beginTask("Starting Analysis...", taskCount.get() * 2);
        try {
            // TODO make numberOfAnalyzers a parameter depending on the turned on analyzer
            // engines.
            int numberOfAnalyzers = 2;
            config.getCodeChecker().analyze(logFile, true, monitor, taskCount.get() * numberOfAnalyzers, config,
                    skipFile);
        } catch (NullPointerException e) {
            Logger.log(IStatus.ERROR, "Could not complete the analysis");
            if (projectView != null)
                projectView.refresAsync(projectView::setEmptyMsg);
        }
        if (projectView != null)
            projectView.refresAsync(projectView::setEmptyMsg);
        return Status.OK_STATUS;
    }
    
    public void createSkipFile() {
        StringBuilder sb = new StringBuilder();
        sb.append("+").append(fileToAnalyze.toAbsolutePath().toString()).append(System.lineSeparator());
        sb.append("+").append("*.hpp").append(System.lineSeparator());
        sb.append("+").append("*.h").append(System.lineSeparator());
        sb.append("+").append("*.hh").append(System.lineSeparator());
        sb.append("+").append("*.hxx").append(System.lineSeparator());
        sb.append("-").append(project.getLocationPrefix()).append("*").append(System.lineSeparator());
        sb.append("-").append("/*");
        try {
            skipFile = Files.createTempFile("skipFile", null);
            Files.write(skipFile, sb.toString().getBytes(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void canceling() {
        config.getCodeChecker().cancelAnalyze();
        project.deleteTemporaryLogFile();
        if (projectView != null)
            projectView.refresAsync(projectView::setEmptyMsg);
    }

}
