package org.codechecker.eclipse.plugin.config.filter;

import org.codechecker.eclipse.plugin.Logger;
import org.codechecker.eclipse.plugin.report.Severity;

import org.eclipse.core.runtime.IStatus;

public class Filter {

    private String filepath = "";
    private String checkerMsg = "";
    private Severity severity = Severity.ANY;
    private String checkerId = "";
    private String buildTarget = "";
    private boolean showSuppressedErrors = false;

    public Filter() {
    }

    public Filter(String filepath, String checkerMsg, Severity severity, String checkerId, String
            buildTarget, boolean showSuppressedErrors) {
        super();
        this.filepath = filepath;
        this.checkerMsg = checkerMsg;
        this.severity = severity;
        this.checkerId = checkerId;
        this.buildTarget = buildTarget;
        this.showSuppressedErrors = showSuppressedErrors;        
    }

    public Filter dup() {
        return new Filter(filepath, checkerMsg, severity, checkerId, buildTarget,
                showSuppressedErrors);
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getCheckerMsg() {
        return checkerMsg;
    }

    public void setCheckerMsg(String checkerMsg) {
        this.checkerMsg = checkerMsg;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getCheckerId() {
        return checkerId;
    }

    public void setCheckerId(String checkerId) {
        this.checkerId = checkerId;
    }

    public String getBuildTarget() {
        return buildTarget;
    }

    public void setBuildTarget(String buildTarget) {
        this.buildTarget = buildTarget;
    }

    public boolean isShowSuppressedErrors() {
        return showSuppressedErrors;
    }

    public void setShowSuppressedErrors(boolean showSuppressedErrors) {
        this.showSuppressedErrors = showSuppressedErrors;
    }


}
