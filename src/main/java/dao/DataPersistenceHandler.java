package dao;

import java.util.List;

/**
 * Created by ashan on 2017-05-03.
 */
public interface DataPersistenceHandler {

    <M> boolean insertRecord(M data) throws Exception;

    <M> boolean deleteRecord(M data) throws Exception;

    <M> boolean updateRecord(M data) throws Exception;

    List readData() throws Exception;

    List readData(String query) throws Exception;

}
