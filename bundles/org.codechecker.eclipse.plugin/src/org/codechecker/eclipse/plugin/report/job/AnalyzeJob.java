package org.codechecker.eclipse.plugin.report.job;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.codechecker.eclipse.plugin.Logger;
import org.codechecker.eclipse.plugin.config.CcConfigurationBase;
import org.codechecker.eclipse.plugin.config.CodeCheckerContext;
import org.codechecker.eclipse.plugin.config.project.CodeCheckerProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * This class represents a CodeChecker analyze command.
 *
 */
public class AnalyzeJob extends Job {

    private CodeCheckerProject project;
    private CcConfigurationBase config;
    private Path logFile;
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
        super("Running CodeChecker Analyze");
        this.project = CodeCheckerContext.getInstance().getCcProject(project);
        config = this.project.getCurrentConfig();
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        Logger.log(IStatus.INFO, "Running AnalyzeJob");
        try {
            copyLogFile();
        } catch (IOException e) {
            Logger.log(IStatus.ERROR, "Couldn't copy logfile!");
        }

        AtomicInteger taskCount = new AtomicInteger(0);

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
        }

        monitor.beginTask("Starting Analysis...", taskCount.get() * 2);
        try {
            // TODO make numberOfAnalyzers a parameter depending on the turned on analyzer
            // engines.
            int numberOfAnalyzers = 2;
            config.getCodeChecker().analyze(logFile, true, monitor, taskCount.get() * numberOfAnalyzers, config);
        } catch (NullPointerException e) {
            Logger.log(IStatus.ERROR, "Could not complete the analysis");
        }

        deleteLogFile();
        return Status.OK_STATUS;
    }
    
    /**
     * Creates a copy of the log file created by ld logger, to avoid concurrency
     * issues.
     * 
     * @throws IOException
     *             Thrown when the copying fails.
     */
    private void copyLogFile() throws IOException {
        Path originalLogFile = project.getLogFileLocation();
        logFile = Paths.get(originalLogFile.toAbsolutePath().toString() + System.nanoTime());
        Files.copy(originalLogFile, logFile);
    }

    /**
     * Delete the temporary logfile after the analysis.
     */
    public void deleteLogFile() {
        try {
            Files.delete(logFile);
        } catch (IOException e) {
            Logger.log(IStatus.ERROR, "Couldn't delete the temporary log file!");
        }
    }
}
