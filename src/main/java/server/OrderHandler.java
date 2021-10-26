package server;

import dao.DataPersistenceHandler;
import dao.OrderDataPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.*;
import util.enums.OrderType;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/*
*
 * Created by ashan on 17/04/03.
 */

public class OrderHandler implements Callable, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderHandler.class);

    private static final int HB_INTERVAL = PropertyReader.getPropertyAsInteger("hb.interval", "5000");
    private transient Socket client;
    private DataInputStream inputStream = null;
    private BufferedWriter outputWriter = null;
    private boolean suspend = false;
    private HashMap<String, OrderBook> orderBookMap;
    private DataPersistenceHandler orderDataPersistence;
    private String line;
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static final String SYSTEM_TRADING_DATE = PropertyReader.getProperty("system.trading.date");


    public OrderHandler(Socket client) {
        this.client = client;
        HeartBeatScheduler scheduledTask = new HeartBeatScheduler();
        orderBookMap = new HashMap<>();
        synchronizeOpenOrders();
        Timer timer = new Timer();
        timer.schedule(scheduledTask, new Date(), HB_INTERVAL);
    }

    private void synchronizeOpenOrders() {
        orderDataPersistence = new OrderDataPersistence();
        try {
            String query = String.format("SELECT * FROM exchange_order WHERE order_time >= %d AND istrade = %s  ORDER BY order_time ASC",
                    dateFormat.parse(SYSTEM_TRADING_DATE).getTime(), false);

            LOGGER.debug("Synchronize order file with current snapshot {}", query);
            List<Order> recoveryOrders = orderDataPersistence.readData(query);

            recoveryOrders.forEach(
                    order -> {
                        if (orderBookMap.containsKey(order.getSymbol())) {
                            OrderBook book = orderBookMap.get(order.getSymbol());

                            if (order.getOrderType() == OrderType.BUY)
                                book.addBuyOrder(order, true);
                            else
                                book.addSellOrder(order, true);

                            LOGGER.debug("new order added to the order book {}", order);
                        } else {
                            OrderBook book = new OrderBook(this, order.getSymbol());
                            if (order.getOrderType() == OrderType.BUY)
                                book.addBuyOrder(order,true);
                            else
                                book.addSellOrder(order,true);
                            orderBookMap.put(order.getSymbol(), book);
                            LOGGER.debug("new order added to the order book {}", order);
                        }
                    }
            );
        } catch (Exception e) {
            LOGGER.error("Unable to synchronize with open orders from database {}", e.getMessage());
        }

    }

    @Override
    public Object call() throws Exception {
        try {
            inputStream = new DataInputStream(client.getInputStream());
            outputWriter = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

            ExecutorService pool = Executors.newFixedThreadPool(
                    PropertyReader.getPropertyAsInteger("system.thread.pool.size", "5")
            );
            while (!suspend) {

                try {
                    line = inputStream.readUTF();
                    LOGGER.debug("new order messages are received {}", line);
                    try {
                        Order ord = MessageParser.decodeMessage(line, Order.class);
                        System.out.println("Order received ");
                        LOGGER.debug("New Order received {}", ord.getOrdID());
                        CompletableFuture.supplyAsync(() -> {
                            if (orderBookMap.containsKey(ord.getSymbol())) {
                                OrderBook book = orderBookMap.get(ord.getSymbol());

                                if (ord.getOrderType() == OrderType.BUY)
                                    book.addBuyOrder(ord);
                                else
                                    book.addSellOrder(ord);

                                LOGGER.debug("new order added to the order book {}", ord);
                                return book;

                            } else {
                                OrderBook book = new OrderBook(this, ord.getSymbol());
                                if (ord.getOrderType() == OrderType.BUY)
                                    book.addBuyOrder(ord);
                                else
                                    book.addSellOrder(ord);
                                orderBookMap.put(ord.getSymbol(), book);
                                LOGGER.debug("new order added to the order book {}", ord);
                                return book;
                            }

                        }, pool);

                    } catch (Exception e) {
                        DataMessage dataMessage = MessageParser.decodeMessage(line, DataMessage.class); //TODO reconnect
                    }
                } catch (Exception e) {
                    LOGGER.error("Connection Error : " + e.getMessage());
                    break;
                }

            }

        } catch (Exception e) {
            LOGGER.error("Server socket error occurred " + e.getMessage());
        }
        return null;
    }

    private void orderRequestManager(ExecutorService pool, Order ord) {
        System.out.println("Order received ");
        LOGGER.debug("New Order received {}", ord.getOrdID());
        CompletableFuture.supplyAsync(() -> {
            if (orderBookMap.containsKey(ord.getSymbol())) {
                OrderBook book = orderBookMap.get(ord.getSymbol());

                if (ord.getOrderType() == OrderType.BUY)
                    book.addBuyOrder(ord);
                else
                    book.addSellOrder(ord);

                LOGGER.debug("new order added to the order book {}", ord);
                return book;

            } else {
                OrderBook book = new OrderBook(this, ord.getSymbol());
                if (ord.getOrderType() == OrderType.BUY)
                    book.addBuyOrder(ord);
                else
                    book.addSellOrder(ord);
                orderBookMap.put(ord.getSymbol(), book);
                LOGGER.debug("new order added to the order book {}", ord);
                return book;
            }

        }, pool);
    }

    private <M> void send(M message) throws Exception {
        outputWriter.write(MessageParser.encodeMessage(message) + '\n');
        outputWriter.flush();
        LOGGER.debug("server process message send {}", message.toString());
    }

    public boolean isSuspend() {
        return suspend;
    }

    public void setSuspend(boolean suspend) {
        this.suspend = suspend;
    }

    public void stopHandler() {
        if (client != null && client.isConnected()) {
            try {
                send(
                        new SystemFTMessage()
                                .setRejectCode(3)
                                .setState("DOWN")
                                .setMessage("System Down")
                );
                setSuspend(true);
                System.out.println("Client channel forcefully suspend by server");
                LOGGER.debug("Client channel forcefully suspend by server");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

/*    public void reconnectHandler() {
        if (isSuspend()) try {
            send(new SystemFTMessage()
                    .setMessage("System Changing")
                    .setState("CHANGE")
                    .setRejectCode(1)
            );
            CompletableFuture.supplyAsync(() -> {
                        orderBookMap.values().forEach(ordBook -> {
                            try {
                                ordBook.recoverBook();
                                System.out.println("new Order recovered" + ordBook.bookName);
                            } catch (Exception e) {
                                LOGGER.debug("unable to recover order book {}", e.getMessage());
                            }
                        });
                        return true;
                    }
            ).thenAccept(result -> {
                try {
                    send(new SystemFTMessage()
                            .setMessage("System Ready")
                            .setState("READY")
                            .setRejectCode(0)
                    );
                } catch (Exception e) {
                    LOGGER.debug("unable to send order response");
                }

            });
            suspend = false;
            LOGGER.debug("Client Session is terminated by server");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    protected void notifyTrades(Trade trade) throws Exception {
        outputWriter.write(MessageParser.encodeMessage(trade) + '\n');
        outputWriter.flush();
        LOGGER.debug("Trade report send {}", trade.getTradeId());
    }

    private class HeartBeatScheduler extends TimerTask {
        private SystemFTMessage hbMessage = new SystemFTMessage("System HeartBeat Message", 4, "HEARTBEAT");

        @Override
        public void run() {
            try {
                if (!isSuspend()) {
                    send(hbMessage);
                    LOGGER.debug("server heartbeat published");


                }
            } catch (Exception e) {
                LOGGER.debug("unable to send scheduled hb message {}", e.getMessage());
            }
        }
    }
}
