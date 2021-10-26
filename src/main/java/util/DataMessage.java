package util;


import util.enums.MessageState;
import util.enums.MessageType;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by ashan on 17/04/04.
 */
public class DataMessage {

    private String message;
    private String messageID;
    private MessageType type;
    private MessageState state;
    private String acknowledgeMessage;
    private int seqNo;

    public int getSeqNo() {
        return seqNo;
    }

    public DataMessage setSeqNo(int seqNo) {
        this.seqNo = seqNo;
        return this;
    }

    public MessageType getType() {
        return type;
    }

    public MessageState getState() {
        return state;
    }

    public DataMessage setState(MessageState state) {
        this.state = state;
        return this;
    }

    public DataMessage setType(MessageType type) {
        this.type = type;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public DataMessage setMessage(String message) {
        this.message = message;
        return this;
    }

    public DataMessage(String message) {
        this.message = message;
        this.type = MessageType.NORMAL;
        setState(MessageState.INITIAL);
    }

    public DataMessage setMessageID(String messageID) {
        this.messageID = messageID;
        return this;
    }

    public String getMessageID() {
        return messageID;
    }

    public DataMessage(String message, MessageType messageType) {
        this.message = message;
        this.type = messageType;
        setState(MessageState.INITIAL);
    }

    public String getAcknowledgeMessage() {
        return acknowledgeMessage;
    }

    public DataMessage setAcknowledgeMessage(String acknowledgeMessage) {
        this.acknowledgeMessage = acknowledgeMessage;
        return this;
    }

    @Override
    public String toString() {
        return String.format("Message ID %s Message : %s Action : %s", messageID, message, type.getDefinition());
    }


   /* public static void main(String args[]) throws UnknownHostException {
        InetAddress addr = InetAddress.getLocalHost();
        String ipAddress = addr.getHostAddress();
        System.out.println("IP address of localhost : " + ipAddress);
        String hostname = addr.getHostName();
        System.out.println("Name of hostname : " + hostname);

        InetAddress address = InetAddress.getByName("localhost");
        System.out.println(address.getHostAddress());
        System.out.println(address.getHostName());

    }*/
}
