package util;

/**
 * Created by ashan on 2017-05-04.
 */
public class SystemFTMessage {
    private String message;
    private int rejectCode;
    private String state;

    public String getState() {
        return state;
    }

    public SystemFTMessage setState(String state) {
        this.state = state;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public SystemFTMessage setMessage(String message) {
        this.message = message;
        return this;
    }

    public int getRejectCode() {
        return rejectCode;
    }

    public SystemFTMessage setRejectCode(int rejectCode) {
        this.rejectCode = rejectCode;
        return this;

    }

    @Override
    public String toString() {
        return String.format("Message : %s RejectCode : %d State : %s",
                getMessage(), getRejectCode(), getState());
    }

    public SystemFTMessage() {
    }

    public SystemFTMessage(String message, int rejectCode, String state) {

        this.message = message;
        this.rejectCode = rejectCode;
        this.state = state;
    }
}
