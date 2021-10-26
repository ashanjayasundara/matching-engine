package util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.enums.MessageType;

/**
 * Created by ashan on 17/04/06.
 */
public class MessageProcessor {
    private DataMessage message = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProcessor.class);

    public MessageProcessor(DataMessage message) {
        this.message = message;
        LOGGER.debug("New message received for processor : " + message.getMessageID());
    }

    public DataMessage execute() {
        int letterCount = message.getMessage().length();
        LOGGER.debug("Message processor start to process message : " + message.getMessageID());
        message.setMessage(
                String.format("Message Processed : [%s] has [%d] characters",
                        message.getMessage(), letterCount)
        ).setType(MessageType.RESPONSE);
        LOGGER.debug("Message processing completed for message : " + message.getMessageID());
        return message;
    }
}
