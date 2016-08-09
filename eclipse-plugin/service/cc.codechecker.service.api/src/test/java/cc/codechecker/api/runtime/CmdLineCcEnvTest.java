package cc.codechecker.api.runtime;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;

/**
 * Command line helper: prints the environment check output for a given cc installation
 */
public class CmdLineCcEnvTest {

    public static void main(String[] argv) {
        String location = "/home/dutow/codechecker_package/CodeChecker";

        try {

            CodeCheckEnvironmentChecker ccec = new CodeCheckEnvironmentChecker(Optional.of
                    ("/home/dutow/checker_env"), location, "/home/dutow/codechecker_workspace", "");

            System.out.println("Environment changes");
            System.out.println("===================");
            for (EnvironmentDifference d : ccec.environmentDifference) {
                System.out.println(d);
            }

            System.out.println("Server stuff");
            System.out.println("===================");

            CodecheckerServerThread thr = new CodecheckerServerThread();
            thr.setCodecheckerEnvironment(ccec);

            thr.start();

            thr.recheck();


            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
}
