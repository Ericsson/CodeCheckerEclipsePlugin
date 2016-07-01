package cc.ecl.action;

/**
 * Used by PerServerActionRunner
 */
public interface ActionRunnerFactory<CommT> {

    public ActionRunner<CommT> createNewInstance(CommT instance);

}
