package cc.ecl.action;

import com.google.common.base.Optional;

import java.util.HashMap;

/**
 * Stores information about action implementations
 *
 * @see ActionRunner
 */
public class ActionImplementationRegistry<CommT> {

    Optional<ActionRunner<CommT>> actionRunner;

    private HashMap<ActionParameterInfo, ActionCommImpl<?, ?, CommT>> implementations;

    public ActionImplementationRegistry() {
        implementations = new HashMap<ActionParameterInfo, ActionCommImpl<?, ?, CommT>>();
        actionRunner = Optional.absent();
    }

    /**
     * Duplicates the object, without it's association to the runner. Used by multi-runner
     * implementations.
     */
    public ActionImplementationRegistry<CommT> dup() {
        ActionImplementationRegistry<CommT> other = new ActionImplementationRegistry<CommT>();
        for (ActionParameterInfo api : implementations.keySet()) {
            other.addImplementation(implementations.get(api).dup());
        }

        return other;
    }

    public Optional<ActionImpl> getImplementationFor(ActionParameterInfo parameterInfo) {
        ActionImpl ai = implementations.get(parameterInfo);

        if (ai == null) {
            return Optional.absent();
        } else {
            return Optional.of(ai);
        }
    }

    public synchronized void addImplementation(ActionCommImpl<?, ?, CommT> implementation) {

        if (implementations.containsKey(implementation.getParameterInfo())) {
            throw new IllegalArgumentException("Already contains an implementation for parameters" +
                    " " + implementation.getParameterInfo().toString());
        }

        implementations.put(implementation.getParameterInfo(), implementation);

        if (actionRunner.isPresent()) {
            implementation.setCommunicationInterface(actionRunner.get().getCommunicationInterface
                    ());
        }
    }

    /**
     * Associates the registry with an ActionRunner. Requires because the action implementations
     * require a fixed communication instance.
     */
    public synchronized void registerToRunner(ActionRunner<CommT> ar) {

        if (actionRunner.isPresent()) {
            throw new RuntimeException("AIR already registered to a runner");
        }

        actionRunner = Optional.of(ar);
        CommT iface = ar.getCommunicationInterface();
        for (ActionCommImpl<?, ?, CommT> impl : implementations.values()) {
            impl.setCommunicationInterface(iface);
        }
    }
}
