package org.codechecker.eclipse.plugin.report.job;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;

/**
 * Reducer Class for JobChangeListener interface.
 *
 */
public abstract class JobDoneChangeListener implements IJobChangeListener {

    @Override
    public abstract void done(IJobChangeEvent event);

    @Override
    public void aboutToRun(IJobChangeEvent arg0) {}

    @Override
    public void awake(IJobChangeEvent arg0) {}

    @Override
    public void running(IJobChangeEvent arg0) {}

    @Override
    public void scheduled(IJobChangeEvent arg0) {}

    @Override
    public void sleeping(IJobChangeEvent arg0) {}
    
}
