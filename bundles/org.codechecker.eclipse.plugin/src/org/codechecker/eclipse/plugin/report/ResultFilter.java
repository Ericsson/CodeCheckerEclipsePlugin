package org.codechecker.eclipse.plugin.report;

import org.codechecker.eclipse.plugin.report.Severity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;

import java.util.Objects;

public class ResultFilter {
    private final Optional<String> filepath;
    private final Optional<String> checkerMsg;
    private final Severity severity;
    private final Optional<String> checkerId;
    private final Optional<String> buildTarget;
    private final boolean showSuppressedErrors;

    public ResultFilter(Optional<String> filepath, Optional<String> checkerMsg, 
    		Severity severity, Optional<String> checkerId, Optional<String> buildTarget, boolean
            showSuppressedErrors) {
        this.filepath = filepath;
        this.checkerMsg = checkerMsg;
        this.severity = severity;
        this.checkerId = checkerId;
        this.buildTarget = buildTarget;
        this.showSuppressedErrors = showSuppressedErrors;
    }

    public Optional<String> getFilepath() {
        return filepath;
    }

    public Optional<String> getCheckerMsg() {
        return checkerMsg;
    }

    public org.codechecker.eclipse.plugin.report.Severity getSeverity() {
        return severity;
    }

    public Optional<String> getCheckerId() {
        return checkerId;
    }

    public Optional<String> getBuildTarget() {
        return buildTarget;
    }

    public boolean isShowSuppressedErrors() {
        return showSuppressedErrors;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("filepath", filepath).add("checkerMsg",
                checkerMsg).add("severity", severity).add("checkerID", checkerId).add
                ("buildTarget", buildTarget).add("suppressed", showSuppressedErrors).toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(filepath, checkerMsg, severity, checkerId, buildTarget,
                showSuppressedErrors);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ResultFilter) {
            ResultFilter oth = (ResultFilter) obj;

            return Objects.equals(filepath, oth.filepath) && Objects.equals(checkerMsg, oth
                    .checkerMsg) && Objects.equals(severity, oth.severity) && Objects.equals
                    (checkerId, oth.checkerId) && Objects.equals(buildTarget, oth.buildTarget) &&
                    Objects.equals(showSuppressedErrors, oth.showSuppressedErrors);
        }
        return false;
    }
}
