package dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Order;
import util.enums.OrderType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ashan on 2017-05-03.
 */
public class OrderDataPersistence extends DatabaseConnection implements DataPersistenceHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderDataPersistence.class);
    private static final String DB_TABLE_NAME = "exchange_order";

    @Override
    public <M> boolean insertRecord(M data) throws Exception {
        if (data instanceof Order) {
            Order order = (Order) data;
            String query = String.format("INSERT INTO %s(side,price,symbol,quantity,userid,orderID,order_time,istrade) values('%s',%s,'%s',%s,'%s','%s','%s',%b)",
                    DB_TABLE_NAME,
                    order.getOrderType(),
                    order.getPrice(),
                    order.getSymbol(),
                    order.getQuantity(),
                    order.getUser(),
                    order.getOrdID(),
                    order.getTime(),
                    order.isTraded()
            );
            return executeQuery(query);
        } else {
            LOGGER.debug("Invalid data type ");
            return false;
        }

    }

    @Override
    public <M> boolean deleteRecord(M data) throws Exception {
        if (data instanceof Order) {
            Order order = (Order) data;
            String query = String.format("DELETE FROM %s WHERE orderID = '%s' ",
                    DB_TABLE_NAME,
                    order.getOrdID()
            );
            return executeQuery(query);
        } else {
            LOGGER.debug("Invalid data type ");
            return false;
        }
    }

    @Override
    public <M> boolean updateRecord(M data) throws Exception {
        //TODO need to implement
        if (data instanceof Order) {
            Order order = (Order) data;
            String query = String.format(
                    "UPDATE %s " +
                            "SET  quantity=%s,istrade=%b " +
                            "WHERE orderID='%s' ",
                    DB_TABLE_NAME,
                    order.getQuantity(),
                    order.isTraded(),
                    order.getOrdID()
            );
            return executeQuery(query);
        } else {
            LOGGER.debug("Invalid data type ");
            return false;
        }
    }

    @Override
    public List readData() throws Exception {
        String query = String.format("SELECT * FROM %s", DB_TABLE_NAME);
        List<Order> orderList = new ArrayList<>();

        resultSet = retrieveData(query);
        while (resultSet.next()) {
            Order order = new Order();

            order.setOrderType(OrderType.getOrderSide(resultSet.getString("side")));
            order.setPrice(resultSet.getDouble("price"));
            order.setSymbol(resultSet.getString("symbol"));
            order.setQuantity(resultSet.getInt("quantity"));
            order.setUser(resultSet.getString("userid"));
            order.setOrdID(resultSet.getString("orderID"));
            order.setTime(resultSet.getLong("order_time"));
            orderList.add(order);

        }
        return orderList;

    }

    @Override
    public List readData(String query) throws Exception {
        LOGGER.debug("Read Order table with custom query {}", query);
        List<Order> orderList = new ArrayList<>();

        resultSet = retrieveData(query);
        while (resultSet.next()) {
            Order order = new Order();

            order.setOrderType(OrderType.getOrderSide(resultSet.getString("side")));
            order.setPrice(resultSet.getDouble("price"));
            order.setSymbol(resultSet.getString("symbol"));
            order.setQuantity(resultSet.getInt("quantity"));
            order.setUser(resultSet.getString("userid"));
            order.setOrdID(resultSet.getString("orderID"));
            order.setTime(resultSet.getLong("order_time"));
            order.setTraded(resultSet.getBoolean("istrade"));

            orderList.add(order);

        }
        return orderList;

    }

}
