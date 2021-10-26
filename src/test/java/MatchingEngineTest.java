import distribution.SystemConfigManagerConnection;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import server.MatchingEngine;
import server.Server;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.Thread.sleep;

/**
 * Created by ashan on 2017-04-28.
 */
public class MatchingEngineTest {
    @Test
    public void test1() throws Exception {
        Server dataServer = MatchingEngine.getServerInstance();
        dataServer.startServer();
        System.out.println("Matching Engine Started");
        SystemConfigManagerConnection connection =SystemConfigManagerConnection.getConnection();

        System.out.println(connection.serviceLookup("registry.matching.engine.service.ip"));

        showMessage("Matching Engine Started");
    }

    private void showMessage(String text) {
        JOptionPane optionPane = new JOptionPane(text, JOptionPane.CLOSED_OPTION);
        JDialog dialog = optionPane.createDialog("Alert");
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
    }

    @Test
    public void test2() throws Exception{

        Server dataServer = MatchingEngine.getServerInstance();
        dataServer.startServer();

        System.out.printf("System started");
        sleep(2000);
        showMessage("Matching Engine up and running");
    }

    @Test
    public void test3() throws Exception{

        Server dataServer = MatchingEngine.getServerInstance();
        dataServer.startServer();

        System.out.printf("System started");
        sleep(2000);
        showMessage("connect new user and make a new order");
        dataServer.stopServer();
        System.out.println("System force stoped");

        showMessage("wait for hb warning");
        sleep(2000);
        dataServer.startServer();

        showMessage("make the trade");
    }
}