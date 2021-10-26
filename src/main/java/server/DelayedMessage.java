package server;


import util.DataMessage;

import java.util.List;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Created by ashan on 2017-04-25.
 */
public class DelayedMessage implements Delayed {

    private long delayTime = 0;
    private List<DataMessage> messageList;

    public List<DataMessage> getMessageList() {
        return messageList;
    }

    public DelayedMessage(long delayTime, List<DataMessage> messagesList) {
        this.delayTime = delayTime;
        this.messageList = messagesList;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(delayTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        if (delayTime < ((DelayedMessage) o).delayTime) {
            return -1;
        } else if (delayTime > ((DelayedMessage) o).delayTime) {
            return 1;
        }
        return 0;
    }
}
