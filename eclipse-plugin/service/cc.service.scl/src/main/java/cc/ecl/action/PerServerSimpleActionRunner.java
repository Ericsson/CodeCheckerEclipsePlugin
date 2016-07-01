package cc.ecl.action;

/**
 * PerServer runner using the simple runner as a backend
 */
public class PerServerSimpleActionRunner<CommT> extends PerServerActionRunner<CommT> {

    public PerServerSimpleActionRunner(CommT communicationInterface, final
    ActionImplementationRegistry implementationRegistry) {
        super(communicationInterface, new ActionRunnerFactory<CommT>() {
            @Override
            public ActionRunner<CommT> createNewInstance(CommT instance) {
                return new SimpleActionRunner<CommT>(instance, implementationRegistry.dup());
            }
        });
    }
}
