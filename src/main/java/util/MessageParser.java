package util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.sun.javafx.property.adapter.PropertyDescriptor;
import com.sun.org.apache.xpath.internal.operations.Or;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

/**
 * Created by ashan on 17/04/04.
 */
public class MessageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageParser.class);

    public static <M> String encodeMessage(M message) {
        LOGGER.debug("Encoding message : " + message);
        Gson jsonParser = new Gson();
        return jsonParser.toJson(message);
    }

    public static <M> M decodeMessage(String message, Class<M> mClass) {
        LOGGER.debug("Decoding message : " + message);
        Gson jsonParser = new Gson();
        JsonReader reader = new JsonReader(new StringReader(message));
        reader.setLenient(true);
        return jsonParser.fromJson(message, mClass);
    }


    public static List decodeMessageAsList(String message) { //TODO need to validate
        LOGGER.debug("Decoding message list : " + message);
        Gson jsonParser = new Gson();
        Type collectionType = new TypeToken<List<Order>>(){}.getType();

        return jsonParser.fromJson(message,collectionType);

    }

    public static String encodeMessageAsList(Collection<Order> message ) {
        LOGGER.debug("Encoding message : " + message);
        Gson jsonParser = new Gson();
        return jsonParser.toJson(message, new TypeToken<List<Order>>() {
        }.getType());
    }

    public static void main(String[] args) {
        MessageParser.decodeMessageAsList("sdsadbjasbfhjsafhjadfjbhas");
    }
}
