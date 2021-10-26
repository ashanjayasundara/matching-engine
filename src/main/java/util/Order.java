package util;

import util.enums.OrderType;

/**
 * Created by ashan on 2017-04-28.
 */
public class Order {
    private OrderType orderType;
    private String symbol;
    private double price;
    private int quantity;
    private long time;
    private String user;

    private boolean traded;

    private String ordID;

    public Order() {
        generateOrderID();
        setTraded(false);
    }

    public String getOrdID() {
        return ordID;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public OrderType getOrderType() {

        return orderType;
    }

    public void setOrdID(String ordID) {
        this.ordID = ordID;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    private void generateOrderID() {
        ordID = getUser() + ":" + System.nanoTime();
    }

    public boolean isTraded() {
        return traded;
    }

    public Order setTraded(boolean traded) {

        this.traded = traded;
        return  this;
    }
}
