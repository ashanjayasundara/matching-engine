package dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Trade;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ashan on 2017-05-04.
 */
public class TradeDataPersistence extends DatabaseConnection implements DataPersistenceHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(TradeDataPersistence.class);
    private static final String DB_TABLE_NAME = "exchange_trade";

    @Override
    public <M> boolean insertRecord(M data) throws Exception {
        if (data instanceof Trade) {
            Trade trade = (Trade) data;
            String query = String.format("INSERT INTO %s(buyer,seller,price,quantity,symbol,trade_time,tradeID) values('%s','%s',%s,%s,'%s','%s','%s')",
                    DB_TABLE_NAME,
                    trade.getBuyer(),
                    trade.getSeller(),
                    trade.getPrice(),
                    trade.getQuantity(),
                    trade.getSymbol(),
                    trade.getTransactionTime(),
                    trade.getTradeId()
            );
            return executeQuery(query);
        } else {
            LOGGER.debug("Invalid data type ");
            return false;
        }
    }

    @Override
    public <M> boolean deleteRecord(M data) throws Exception {
        if (data instanceof Trade) {
            Trade trade = (Trade) data;
            String query = String.format("DELETE FROM %s WHERE tradeID = %s",
                    DB_TABLE_NAME,
                    trade.getTradeId()
            );
            return executeQuery(query);
        } else {
            LOGGER.debug("Invalid data type ");
            return false;
        }
    }

    @Override
    public <M> boolean updateRecord(M data) throws Exception {
        return false;
    }

    @Override
    public List readData() throws Exception {
        String query = String.format("SELECT * FROM %s", DB_TABLE_NAME);
        List<Trade> tradeList = new ArrayList<>();

        resultSet = retrieveData(query);
        while (resultSet.next()) {
            Trade trade = new Trade();

            trade.setBuyer(resultSet.getString("buyer"));
            trade.setSeller(resultSet.getString("seller"));
            trade.setPrice(resultSet.getDouble("price"));
            trade.setSymbol(resultSet.getString("symbol"));
            trade.setQuantity(resultSet.getInt("quantity"));
            trade.setTradeId(resultSet.getString("tradeID"));
            trade.setTransactionTime(resultSet.getLong("trade_time"));
            tradeList.add(trade);

        }
        return tradeList;
    }

    @Override
    public List readData(String query) throws Exception {
        List<Trade> tradeList = new ArrayList<>();

        resultSet = retrieveData(query);
        while (resultSet.next()) {
            Trade trade = new Trade();

            trade.setBuyer(resultSet.getString("buyer"));
            trade.setSeller(resultSet.getString("seller"));
            trade.setPrice(resultSet.getDouble("price"));
            trade.setSymbol(resultSet.getString("symbol"));
            trade.setQuantity(resultSet.getInt("quantity"));
            trade.setTradeId(resultSet.getString("tradeID"));
            trade.setTransactionTime(resultSet.getLong("trade_time"));
            tradeList.add(trade);

        }
        return tradeList;
    }


}
