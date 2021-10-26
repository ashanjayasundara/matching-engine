package server;

/**
 * Created by ashan on 17/04/05.
 */
public interface Server {
    void startServer() throws Exception;

    void stopServer() throws Exception;

    void onConnect() throws Exception;

    boolean isRunning() throws Exception;
}
