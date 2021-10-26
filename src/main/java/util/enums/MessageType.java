package util.enums;

/**
 * Created by ashan on 2017-04-11.
 */
public enum MessageType {
    NORMAL(1, "NORMAL"),
    ACKNOWLEDGE(2, "ACKNOWLEDGE"),
    RESPONSE(3, "RESPONSE"),
    DISCONNECT(4, "DISCONNECT"),
    ERROR(5, "ERROR"),
    PENDING(6,"PENDING"),
    HEARTBEAT(7,"HEART BEAT"),
    SERVER_OFFLINE(8, "SERVER OFFLINE"),
    SERVER_ONLINE(9,"SERVER ONLINE");

    private int type;
    private String definition;

    public int getType() {
        return type;
    }

    public String getDefinition() {
        return definition;
    }

    @Override
    public String toString() {
        return getDefinition();
    }

    MessageType(int type, String definition) {
        this.definition = definition;
        this.type = type;

    }
}
