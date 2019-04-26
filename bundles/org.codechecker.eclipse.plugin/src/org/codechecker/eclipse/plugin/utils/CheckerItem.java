package org.codechecker.eclipse.plugin.utils;

public class CheckerItem implements Comparable<CheckerItem>{

    public enum LAST_ACTION {
        NONE, SELECTION, DESELECTION
    };
    String text;
    private LAST_ACTION lastAction;

    public CheckerItem(final String text) {
        this.text = text;
        this.lastAction = LAST_ACTION.NONE;
    }
    
    public CheckerItem(final String text, LAST_ACTION lastaction) {
        this.text = text;
        this.lastAction = lastaction;
    }
    
    public String getText() {
        return this.text;
    }
    
    public LAST_ACTION getLastAction() {
        return this.lastAction;
    }

    public void setLastAction(final LAST_ACTION lastAction) {
        this.lastAction = lastAction;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof CheckerItem) {
            CheckerItem ci = (CheckerItem) o;
            return this.text.equals(ci.getText()) && this.lastAction.equals(ci.getLastAction());
        }
        return false;
    }

    @Override
    public int compareTo(CheckerItem o) {
        return this.text.compareTo(o.getText());
    }
}

