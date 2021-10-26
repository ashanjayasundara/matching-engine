package distribution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.MatchingEngine;
import util.ConfigMessage;
import util.MessageParser;
import util.PropertyReader;
import util.SystemFTMessage;
import util.enums.ConnectionState;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static util.enums.ConnectionState.*;

public class SystemConfigManagerConnection implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemConfigManagerConnection.class);
    private static final int MAX_HB_COUNT = PropertyReader.getPropertyAsInteger("maximum.hb.count", "10");
    private static final int HB_INTERVAL = PropertyReader.getPropertyAsInteger("hb.interval", "5000");
    private static final int SERVER_PORT = PropertyReader.getPropertyAsInteger("server.config.manager.port", "9000");
    private static final int MAX_MSG_RECEIVED_WAIT_TIME = PropertyReader.getPropertyAsInteger("maximum.message.wait.timeout", "5000");

    private static SystemConfigManagerConnection configManagerConnection = null;
    private static String SERVER_NAME;

    private Socket socketClient;
    private BufferedReader bufferedReader;
    private DataOutputStream outputWriter = null;

    private char[] buffer = new char[1024];
    private HeartBeatListener heartBeatListener = null;
    private Timer scheduleTimer = null;
    private int lostHeartBeatCount = 0;
    private List subscribers;
    private HashMap<String, String> cacheConfigurations;
    private ConnectionState connectionState = ConnectionState.OFFLINE;

    static {
        try {
            SERVER_NAME = InetAddress.getByName(
                    PropertyReader.getProperty("server.config.manager.hostname")
            ).getHostAddress();
        } catch (UnknownHostException e) {
            LOGGER.error("Unable to resolve matching engine Hosted IP {}", e.getMessage());
        }
    }

    private SystemConfigManagerConnection() {

        heartBeatListener = new HeartBeatListener();
        subscribers = new ArrayList<>();
        cacheConfigurations = new HashMap<>();

        connect();
        LOGGER.debug("system connection listener initialized successfully");
    }

    public static SystemConfigManagerConnection getConnection() throws Exception {

        if (configManagerConnection == null) {
            configManagerConnection = new SystemConfigManagerConnection();
        }
        return configManagerConnection;
    }

    public synchronized <T> void subscribe(T s) {
        subscribers.add(s);
        LOGGER.debug("New client subscribe to the dynamic update channel");
    }

    public synchronized <T> void unSubscribe(T s) {
        LOGGER.debug("client unSubscribe the dynamic update channel");
        subscribers.remove(s);
    }

    public String serviceLookup(String serviceKey) {
        if (cacheConfigurations.containsKey(serviceKey)) {
            LOGGER.debug("service lookup success {}", serviceKey);
            return cacheConfigurations.get(serviceKey);
        } else {
            String[] result = {""};
            CompletableFuture.supplyAsync(() -> {
                try {
                    if (!cacheConfigurations.containsKey(serviceKey)) {
                        ConfigMessage request = new ConfigMessage()
                                .setRequestType(1)
                                .setConfig(serviceKey)
                                .setModule("Matching Engine");
                        sendMessage(request);
                    }
                } catch (Exception e) {
                    LOGGER.debug("unable to send ");
                }
                return serviceKey;
            }).thenApplyAsync(this::waitForServiceLookup)
                    .thenAccept(
                            value -> result[0] = value
                    );
            return result[0];
        }
    }

    public boolean serviceRegistry(String config, String value) {
        boolean[] result = {false};
        CompletableFuture.supplyAsync(() -> {
            try {
                ConfigMessage request = new ConfigMessage()
                        .setRequestType(0)
                        .setConfig(config)
                        .setValue(value)
                        .setModule("Matching Engine");
                sendMessage(request);

            } catch (Exception e) {
                LOGGER.debug("unable to to service register with Configuration Manager service {}", e.getMessage());
            }
            return config;
        }).thenApplyAsync(this::waitForServiceLookup)
                .thenAccept(
                        x -> result[0] = true
                );
        return result[0];
    }

    private void connect() {
        try {
            socketClient = new Socket(SERVER_NAME, SERVER_PORT);
            outputWriter = new DataOutputStream(socketClient.getOutputStream());
            bufferedReader = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
            socketClient.setReuseAddress(true);

            socketClient.setKeepAlive(true);
            socketClient.setTcpNoDelay(false);
            connectionState = ESTABLISH;

            CompletableFuture.runAsync(this).thenAccept(x -> {
                LOGGER.debug("Server Communication is terminated");
            });

            scheduleTimer = new Timer();
            scheduleTimer.schedule(heartBeatListener, new Date(), HB_INTERVAL);

            LOGGER.info("Connection Listener is established");

        } catch (IOException e) {
            LOGGER.error("Unable to establish connection listener : {} ", e.getMessage());
        }
    }

    private <M> void sendMessage(M message) throws Exception {
        outputWriter.writeUTF(MessageParser.encodeMessage(message));
        outputWriter.flush();
        LOGGER.debug("server process message send {}", message.toString());
    }

    @Override
    public void run() {
        while (!socketClient.isClosed() && socketClient.isConnected()) {
            int count;
            try {
                if ((count = bufferedReader.read(buffer)) > 0) {
                    String line = String.valueOf(buffer, 0, count);
                    try {

                        ConfigMessage configs = MessageParser.decodeMessage(line, ConfigMessage.class);

                        if (configs.getConfig() == null || configs.getConfig().isEmpty())
                            throw new Exception("Message format miss matched");

                        processConfigMessage(configs);
                        System.out.println("New configuration message is received : " + line);

                    } catch (Exception e) {
                        SystemFTMessage ftMessage = MessageParser.decodeMessage(line, SystemFTMessage.class);
                        LOGGER.debug("new FT message received {}", ftMessage.getMessage());
                        switch (ftMessage.getRejectCode()) {
                            case 0:
                                connectionState = ESTABLISH;
                                break;
                            case 1:
                                connectionState = CHANGING;
                                System.out.println("Matching Engine State Change");
                                break;

                            case 2:
                                connectionState = STARTING;
                                break;
                            case 3:
                                connectionState = TERMINATE;
                                break;
                            case 4:
                                lostHeartBeatCount = 0;
                                connectionState = RUNNING;
                                break;
                            default:
                                break;
                        }
                    }

                }
            } catch (Exception e) {
                //TODO Need to handle exception
            }
        }
        System.out.println("Listener Started");
    }

    private void processConfigMessage(ConfigMessage message) {
        if (message.getRequestType() == 2) {// Received configuration  update message
            cacheConfigurations.put(message.getConfig(), message.getValue());
        } else {
            cacheConfigurations.put(message.getConfig(), message.getValue());
            LOGGER.debug("service configuration message received {}", message.toString());
        }
    }

    private void notifyToSubscribers() {
        LOGGER.debug("Send notification to subscribers");
        subscribers.forEach(
                sub -> {
                    if (sub instanceof MatchingEngine) {
                        sub = (MatchingEngine) sub;
                        //   sub. TODO Need to implement
                    }
                }
        );
    }

    private String waitForServiceLookup(String key) {
        long waitStartTime = System.currentTimeMillis();
        String value = null;
        LOGGER.debug("waiting for response receive {}", key);

        while ((System.currentTimeMillis() - waitStartTime) < MAX_MSG_RECEIVED_WAIT_TIME) {
            if (cacheConfigurations.containsKey(key)) {
                value = cacheConfigurations.get(key);
                break;
            }
        }
        return value;
    }

    private class HeartBeatListener extends TimerTask {

        @Override
        public void run() {
            lostHeartBeatCount++;
            if (lostHeartBeatCount > 1 && lostHeartBeatCount < MAX_HB_COUNT) {
                LOGGER.warn("Config Manager Heart Beats Missed {}/{}", lostHeartBeatCount, MAX_HB_COUNT);
                System.out.println("Config Manager Heart Beats Missed : " + lostHeartBeatCount);
                try {
                    connect();
                    LOGGER.info("Config manager response received");
                } catch (Exception e) {
                    LOGGER.debug("matching engine not responding {}", e.getMessage());
                }
            }
            if (lostHeartBeatCount > MAX_HB_COUNT) {
                try {
                    connect();
                    LOGGER.info("matching engine reconnected");
                } catch (Exception e) {
                    LOGGER.debug("matching engine not responding {}", e.getMessage());
                }
            }
        }
    }
}
