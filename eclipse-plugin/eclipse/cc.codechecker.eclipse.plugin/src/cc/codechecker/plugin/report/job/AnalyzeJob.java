package cc.codechecker.plugin.report.job;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.google.common.base.Optional;

import cc.codechecker.plugin.Logger;
import cc.codechecker.plugin.config.CodeCheckerContext;
import cc.codechecker.plugin.runtime.CodeCheckEnvironmentChecker;

/**
 * This class represents a CodeChecker analyze command.
 *
 */
public class AnalyzeJob extends Job {

    private IProject project;
    private CodeCheckEnvironmentChecker ccec;
    
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
        this.project = project;
        ccec = new CodeCheckEnvironmentChecker(CodeCheckerContext.getInstance()
                .getConfigForProject(project).getProjectConfig(null));
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        Logger.log(IStatus.INFO, "Running AnalyzeJob");
        
        Optional<String> analyzeLog = moveLogFile();
        if(analyzeLog.isPresent()) {
            AtomicInteger taskCount = new AtomicInteger(0);
            try {
                //read the file
                Files.lines(Paths.get(analyzeLog.get())).forEach(new Consumer<String>() {

                    @Override
                    public void accept(String t) {
                        if ( t.contains("\"command\":")){
                            taskCount.incrementAndGet();
                        }
                    }
                });
            } catch (IOException e) {
                // isPresent() should render this unneeded. 
            }
            monitor.beginTask("Starting Analysis...", taskCount.get()*2);
            ccec.processLog(analyzeLog.get(), true, monitor, taskCount.get()*2);
        }
        return Status.OK_STATUS;
    }
    
    /**
     * Renames the logfile, to avoid concurrency issues.
     * @return The path to the temporary logfile wrapped in an {@link Optional}.
     */
    private Optional<String> moveLogFile() {
        String filename = CodeCheckerContext.getInstance().getConfigForProject(project).getLogFileLocation();
        File f = new File(filename);
        if (f.exists()) {
            String newName = filename + System.nanoTime();
            f.renameTo(new File(newName));

            return Optional.of(newName);
        }
        return Optional.absent();
    }
}
