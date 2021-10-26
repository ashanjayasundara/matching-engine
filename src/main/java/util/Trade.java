package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ashan on 2017-04-28.
 */
public class Trade {
    private static final Logger LOGGER = LoggerFactory.getLogger(Trade.class);

    private String buyer;
    private String seller;
    private int quantity;
    private double price;
    private long transactionTime;
    private String tradeId;
    private String symbol;

    public void setTransactionTime(long transactionTime) {
        this.transactionTime = transactionTime;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getSymbol() {
        return symbol;
    }

    public Trade setSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public long getTransactionTime() {
        return transactionTime;
    }

    public String getTradeId() {
        return tradeId;
    }

    public String getBuyer() {

        return buyer;
    }

    public Trade setBuyer(String buyer) {
        this.buyer = buyer;
        return this;
    }

    public Trade() {
        transactionTime = System.nanoTime();
        tradeId = "trade:" + transactionTime;
    }

    public String getSeller() {
        return seller;
    }

    public Trade setSeller(String seller) {
        this.seller = seller;
        return this;
    }

    public int getQuantity() {
        return quantity;
    }

    public Trade setQuantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public double getPrice() {
        return price;
    }

    public Trade setPrice(double price) {
        this.price = price;
        return this;
    }
}
