package at.ac.tuwien.dsg.smartcom.adapters;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class FreePortProviderUtil {

    public static int getFreePort() {
        try {
            try (ServerSocket socket = new ServerSocket(0)) {
                socket.setReuseAddress(true);
                return socket.getLocalPort();
            }
        } catch (IOException e) {
            return -1;
        }
    }
}
