package util.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ashan on 2017-04-28.
 */
public enum OrderType {
    BUY("BUY"),
    SELL("SELL");

    private String side;
    private static final Map<String, OrderType> sideLookup = new HashMap<String, OrderType>();

    static {
        for (OrderType orderType : OrderType.values()) {
            sideLookup.put(orderType.getSide(), orderType);
        }
    }

    OrderType(String side) {
        this.side = side;
    }


    public String getSide() {
        return side;
    }

    public static OrderType getOrderSide(String side) {
        return sideLookup.get(side.toUpperCase());
    }
}
