package org.codechecker.eclipse.plugin.utils;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

public abstract class SimpleSelectionAdapter implements SelectionListener {

    @Override
    public void widgetDefaultSelected(final SelectionEvent e) {
        this.handle(e);

    }

    @Override
    public void widgetSelected(final SelectionEvent e) {
        this.handle(e);
    }

    public abstract void handle(SelectionEvent e);

}