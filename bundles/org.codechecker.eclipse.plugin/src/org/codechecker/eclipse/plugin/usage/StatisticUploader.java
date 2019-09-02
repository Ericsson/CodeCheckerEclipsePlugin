package org.codechecker.eclipse.plugin.usage;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.FrameworkUtil;

import com.google.gson.Gson;

/**
 * Class for uploading the usage statistics.
 */
public class StatisticUploader implements Runnable {

    private UsageInfo info;

    /**
     * Need to pass an {@link UsageInfo}.
     * 
     * @param info
     *            This will be uploaded in JSON format.
     */
    public StatisticUploader(UsageInfo info) {
        this.info = info;
    }

    /**
     * This is the upload logic. The host and port is specified compile time in
     * maven.
     */
    private void uploadStatistics() {
        Integer port = null;
        String host = null;

        try (InputStream is = FileLocator.openStream(FrameworkUtil.getBundle(getClass()),
                new Path("resources/config.properties"), false)) {
            Properties prop = new Properties();
            prop.load(is);
            host = prop.getProperty("host");
            try {
                port = Integer.parseInt(prop.getProperty("port"));
            } catch (Exception e) {
                ;
            }
        } catch (IOException e1) {
            ;
        }

        try (DatagramSocket socket = new DatagramSocket()) {
            if (port != null && host != null) {
                DatagramPacket packet = new DatagramPacket(new Gson().toJson(info).getBytes(),
                        new Gson().toJson(info).getBytes().length,
                        Inet4Address.getByName(host), port);
                socket.send(packet);
            }
        } catch (IOException e) {
            ;
        }
    }

    /**
     * Uploads usage statistics.
     */
    @Override
    public void run() {
        uploadStatistics();
    }

}
