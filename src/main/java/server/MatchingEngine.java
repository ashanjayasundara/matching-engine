package server;


import distribution.SystemConfigManagerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.PropertyReader;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Created by ashan on 17/04/03.
 */
public class MatchingEngine extends Thread implements Server, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatchingEngine.class);
    private static final int SERVER_PORT = PropertyReader.getPropertyAsInteger("server.matching.engine.port", "8000");

    private ExecutorService executorService = null;
    private ServerSocket serverSocket;
    private static MatchingEngine matchingEngine = null;
    private boolean runServer = true;
    private List<OrderHandler> clientSessionList = null;
    private SystemConfigManagerConnection dynamicUpdateConnection = null;
    private static final String MATCHING_REGISTRY_CONFIG = PropertyReader.getProperty("matching.engine.registry.host");

    private MatchingEngine() {
        try {
            dynamicUpdateConnection = SystemConfigManagerConnection.getConnection();
            dynamicUpdateConnection.subscribe(this);
        } catch (Exception e) {
            LOGGER.debug("failed to connect dynamic update channel {}", e.getMessage());
        }
    }

    public static MatchingEngine getServerInstance() throws Exception {
        if (matchingEngine == null)
            matchingEngine = new MatchingEngine();
        return matchingEngine;
    }

    @Override
    public void startServer() throws Exception {
        LOGGER.info("Server initialize at port " + SERVER_PORT);
        try {
            if (serverSocket == null) {
                serverSocket = new ServerSocket(SERVER_PORT);
                serverSocket.setReuseAddress(true);
                dynamicUpdateConnection.serviceRegistry(MATCHING_REGISTRY_CONFIG, InetAddress.getLocalHost().getHostAddress());
                clientSessionList = new ArrayList<>();
                this.setName("Server Thread : MatchingEngine");
                this.setDaemon(true);
                this.start();
                onConnect();
            } else {
                onConnect();
                LOGGER.debug("matching engine successfully synchronize with last snapshot");
            }
            LOGGER.info("Server start to listen client messages");
        } catch (Exception e) {
            LOGGER.error("Server Start Failed " + e.getMessage());
            throw e;
        }
        LOGGER.debug("Server initialization success");
    }

    @Override
    public void stopServer() throws Exception {
        if (serverSocket == null) {
            LOGGER.error("Server is not started");
            throw new Exception("Server is not started");
        } else {
            runServer = false;
            clientSessionList.forEach(x -> x.stopHandler());
            runServer = false;
            LOGGER.info("Server successfully stop");
        }
    }

    @Override
    public void onConnect() throws Exception {
        /*runServer = true;
        inputStream = new ObjectInputStream(new FileInputStream(DATA_FILE_NAME));
        while (inputStream.read() != -1) {
            Object object = inputStream.readObject();
            if (object instanceof OrderHandler)
                clientSessionList.add((OrderHandler) object);
            LOGGER.debug("Sysnchronize Data File {}", object.toString());
        }
        inputStream.close();
        clientSessionList.forEach(x -> x.reconnectHandler());*/
        LOGGER.debug("Server comes to online");
    }

    @Override
    public void run() {
        executorService = Executors.newFixedThreadPool(
                PropertyReader.getPropertyAsInteger(
                        "server.matching.pool.size", "5"
                )
        );
        LOGGER.debug("Waiting for client connection");
        try {
            System.out.println("running");
            while (runServer) { //TODO add run server flag
                Socket clientSocket = serverSocket.accept();
                System.out.println("server waiting for new connection");
                LOGGER.debug("new broker connected to matching server {}", clientSocket.getLocalAddress().getHostAddress());
                OrderHandler orderHandler = new OrderHandler(clientSocket);
//                writeToDataFile(orderHandler);
                executorService.execute(new FutureTask(orderHandler));
                clientSessionList.add(orderHandler);
            }
        } catch (IOException s) {
            LOGGER.error(s.getMessage());
        } finally {
            LOGGER.info("Server Execution Successfully Terminated");
            if (executorService != null) {
                executorService.shutdownNow();
                LOGGER.debug(String.format("Server Executor Service %s shutdown ", executorService.isShutdown() ? "successfully" : "failed to"));
            }
        }
    }

    @Override
    public boolean isRunning() throws Exception {
        return serverSocket != null && !serverSocket.isClosed();
    }

    @Override
    public void close() throws Exception {
       /* if (outputStream != null) {
            outputStream.close();
        }
        if (inputStream != null) {
            inputStream.close();
        }*/
        LOGGER.debug("Data File buffers release memory successfully");
    }

   /* private <M> void writeToDataFile(M m) {
        try {
            outputStream = new ObjectOutputStream(new FileOutputStream(DATA_FILE_NAME));
            outputStream.writeObject(m);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

}


