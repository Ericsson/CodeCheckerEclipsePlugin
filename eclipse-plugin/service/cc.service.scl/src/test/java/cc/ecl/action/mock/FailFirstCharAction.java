package cc.ecl.action.mock;


import cc.ecl.action.Action;

public class FailFirstCharAction extends Action<Character, Character> {
    public FailFirstCharAction(Character request) {
        super(request);
    }
}
