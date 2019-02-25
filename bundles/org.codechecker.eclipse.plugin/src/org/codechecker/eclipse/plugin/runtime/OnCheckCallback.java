package org.codechecker.eclipse.plugin.runtime;

public interface OnCheckCallback {
	void analysisStarted(String command);//called when the analysis starts
    void analysisFinished(String result);//called when analysis finished
}
