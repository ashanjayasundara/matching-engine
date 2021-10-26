package server;

import dao.DataPersistenceHandler;
import dao.OrderDataPersistence;
import dao.TradeDataPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Order;
import util.PropertyReader;
import util.Trade;
import util.enums.OrderType;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by ashan on 2017-04-28.
 */
public class OrderBook {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderBook.class);
    private OrderHandler orderHandler;
    public String bookName = null;
    private DataPersistenceHandler tradeDataPersistence;
    private DataPersistenceHandler orderPersistence;
    private SortedSet<Order> buyOrders = new TreeSet<>((o1, o2) -> (int) (o2.getPrice() - o1.getPrice())); // Price sort disending
    private SortedSet<Order> sellOrders = new TreeSet<>(((o1, o2) -> (int) (o1.getPrice() - o2.getPrice()))); // price sort accending
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static final String SYSTEM_TRADING_DATE = PropertyReader.getProperty("system.trading.date");


    public OrderBook(OrderHandler orderHandler, String symbol) {
        this.orderHandler = orderHandler;
        bookName = symbol;
        tradeDataPersistence = new TradeDataPersistence();
        orderPersistence = new OrderDataPersistence();
        LOGGER.debug("order book initialize successfully");
    }

    public synchronized void addBuyOrder(Order order) {
        addBuyOrder(order, false);
    }

    public synchronized void addBuyOrder(Order order, boolean recovery) {
        try {
            buyOrders.add(order);
            if (!recovery)
                orderPersistence.insertRecord(order);
        } catch (Exception e) {
            LOGGER.error("Unable to save order details to database {}", order.getOrdID());
        }
        if (sellOrders.size() > 0)
            doMatching();
    }

    public synchronized void addSellOrder(Order order) {
        addSellOrder(order, false);
    }

    public synchronized void addSellOrder(Order order, boolean recovery) {
        try {
            sellOrders.add(order);
            if (!recovery)
                orderPersistence.insertRecord(order);
        } catch (Exception e) {
            LOGGER.error("Unable to save order details to database {}", order.getOrdID());
        }
        if (buyOrders.size() > 0)
            doMatching();
    }

    private void doMatching() {
        try {
            System.out.println("Start  to matching orders");

            if (sellOrders.first().getPrice() <= buyOrders.first().getPrice())
                if (sellOrders.first().getQuantity() == buyOrders.first().getQuantity()) {
                    Order buy = buyOrders.first();
                    Order sell = sellOrders.first();

                    Trade trade = new Trade()
                            .setBuyer(buy.getUser())
                            .setPrice(sell.getPrice())
                            .setQuantity(buy.getQuantity())
                            .setSeller(sell.getUser())
                            .setSymbol(buy.getSymbol());
                    notifyToListener(trade);

                    tradeDataPersistence.insertRecord(trade);

                    orderPersistence.updateRecord(buy.setTraded(true));
                    orderPersistence.updateRecord(sell.setTraded(true));

                    buyOrders.remove(buy);
                    sellOrders.remove(sell);

                    System.out.println("Trade executed");
                    LOGGER.debug("Trade executed {}", trade.getTradeId());

                } else if (sellOrders.first().getQuantity() > buyOrders.first().getQuantity()) {
                    Order buy = buyOrders.first();
                    Order sell = sellOrders.first();

                    Trade trade = new Trade()
                            .setBuyer(buy.getUser())
                            .setPrice(sell.getPrice())
                            .setQuantity(buy.getQuantity())
                            .setSeller(sell.getUser())
                            .setSymbol(buy.getSymbol());
                    notifyToListener(trade);

                    tradeDataPersistence.insertRecord(trade);
                    orderPersistence.updateRecord(buy.setTraded(true));
                    buyOrders.remove(buy);
                    sellOrders.remove(sell);
                    sell.setQuantity(sell.getQuantity() - buy.getQuantity());
                    sellOrders.add(sell);
                    orderPersistence.updateRecord(sell); // partially traded

                }
        } catch (Exception e) {
            LOGGER.error("unable to matching trades {}", e.getMessage());
        }
    }

    private void notifyToListener(Trade trade) throws Exception {
        orderHandler.notifyTrades(trade);
        LOGGER.debug("Trade has been notified {}", trade.getTradeId());
    }

    public void recoverBook() throws Exception {
        LOGGER.debug("attempt to recover the system failure, open orders are loaded to order book");
        String query = String.format("SELECT * FROM exchange_order WHERE order_time >= %d AND istrade = %s AND symbol = '%s' ORDER BY order_time ASC",
                dateFormat.parse(SYSTEM_TRADING_DATE).getTime(), false, bookName);

        LOGGER.debug(query);

        List<Order> recoveryOrders = orderPersistence.readData(query);
        sellOrders.clear();
        buyOrders.clear();
        LOGGER.debug("Initial order book has been swept");

        recoveryOrders.forEach(
                order -> {
                    if (order.getOrderType() == OrderType.BUY) {
                        buyOrders.add(order);
                        LOGGER.debug("order has been recovered {}", order.getOrdID());
                    } else {
                        sellOrders.add(order);
                        LOGGER.debug("order has been recovered {}", order.getOrdID());
                    }
                }
        );

        LOGGER.debug("Order book {} has been recovered successfully", bookName);
    }
}
