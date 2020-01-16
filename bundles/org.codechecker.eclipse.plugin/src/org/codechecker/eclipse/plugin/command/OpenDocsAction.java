package org.codechecker.eclipse.plugin.command;

import java.net.MalformedURLException;
import java.net.URL;

import org.codechecker.eclipse.plugin.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Action for opening a browser with an arbitrary url.
 */
public class OpenDocsAction extends Action {

    private final String url;

    /**
     * Constructor where you must specify an url.
     * 
     * @param url
     *            The url to be used. Must not be null
     */
    public OpenDocsAction(@NonNull String url) {
        super("View Documentation");
        this.url = url;
    }

    @Override
    public void run() {
        try {
            PlatformUI.getWorkbench().getBrowserSupport().createBrowser("CodeChecker")
                    .openURL(new URL(url));
        } catch (PartInitException | MalformedURLException e) {
            Logger.log(IStatus.ERROR, "Couldn't go to link.");
        }
    }

    /**
     * Holder class for the actual links.
     */
    public final class DocTypes {
        public static final String SA = "https://clang.llvm.org/docs/analyzer/checkers.html";
        public static final String TIDY = "https://clang.llvm.org/extra/clang-tidy/checks/list.html";
    }
}
