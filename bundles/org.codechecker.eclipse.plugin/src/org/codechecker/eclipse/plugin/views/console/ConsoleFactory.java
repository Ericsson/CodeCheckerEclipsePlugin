package org.codechecker.eclipse.plugin.views.console;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class ConsoleFactory implements IConsoleFactory{

    private static MessageConsole console;

    private static MessageConsole getConsole() {
        if(console == null) {
            console = new MessageConsole("CodeChecker Console", null);
        }
        return console;
    }

    public static void consoleWrite(String msg) {
        console = getConsole();
        MessageConsoleStream out = console.newMessageStream();
        out.println(msg);
    }

    public static void setActiveConsole() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        try {
            page.showView(console.getName());
        } catch (PartInitException e) {}
    }
    
    @Override
    public void openConsole() {
        console = getConsole();
        if (console != null) {
            IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
            IConsole[] existing = manager.getConsoles();
            boolean exists = false;
            for (int i = 0; i < existing.length; i++) {
                if(console == existing[i])
                    exists = true;
            }
            if(!exists)
                manager.addConsoles(new IConsole[] {console});
            manager.showConsoleView(console);
        }
    }
}