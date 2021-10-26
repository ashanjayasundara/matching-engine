package util;

import java.util.Properties;

/**
 * Created by ashan on 17/04/05.
 */
public class PropertyReader {

    private static PropertyReader propertyReader = null;
    private static Properties properties;
    private static final String PROPERTY_FILE_NAME = "config.properties";

    private PropertyReader() {
        properties = new Properties();
        try {
            properties.load(PropertyReader.class.getClassLoader().getResourceAsStream(PROPERTY_FILE_NAME));

        } catch (Exception e) {
            System.out.println("error" + e);
        }
    }

    public static String getProperty(String key) {
        getSystemProperties();
        String value = null;
        if (properties != null) {
            value = properties.getProperty(key);
        }
        return value;
    }

    public static String getProperty(String key, String defaultValue) {
        getSystemProperties();
        String value = null;
        if (properties != null) {
            value = properties.getProperty(key, defaultValue);
        }
        return value;
    }

    public static int getPropertyAsInteger(String key) {
        return Integer.parseInt(getProperty(key));
    }

    public static int getPropertyAsInteger(String key, String defaultValue) {
        return Integer.parseInt(getProperty(key, defaultValue));
    }

    public static Double getPropertyAsDouble(String key) {
        return Double.parseDouble(getProperty(key));
    }

    public static Double getPropertyAsDouble(String key, String defaultValue) {
        return Double.parseDouble(getProperty(key, defaultValue));
    }

    public static Boolean getPropertyAsBoolean(String key) {
        return Boolean.valueOf(getProperty(key));
    }

    public static Boolean getPropertyAsBoolean(String key, String defaultValue) {
        return Boolean.valueOf(getProperty(key, defaultValue));
    }

    public static Long getPropertyAsLong(String key) {
        return Long.parseLong(getProperty(key));
    }

    public static Long getPropertyAsLong(String key, String defaultValue) {
        return Long.parseLong(getProperty(key, defaultValue));
    }


    private static void getSystemProperties() {
        if (propertyReader == null) {
            propertyReader = new PropertyReader();
        }
    }
}
