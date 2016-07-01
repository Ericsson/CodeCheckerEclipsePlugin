package cc.codechecker.api.action.analyze;

public class AnalyzeResult {

    private final boolean success;

    public AnalyzeResult(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }
}
