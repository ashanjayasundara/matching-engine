package util;


/**
 * Created by ashan on 17/04/04.
 */
public class ConfigMessage {

    private String module;
    private int requestType;
    private int rejectCode;
    private String config;
    private String value;

    public String getModule() {
        return module;
    }

    public ConfigMessage setModule(String module) {
        this.module = module;
        return this;
    }

    public int getRequestType() {
        return requestType;
    }

    public ConfigMessage setRequestType(int requestType) {
        this.requestType = requestType;
        return this;
    }

    public int getRejectCode() {
        return rejectCode;
    }

    public ConfigMessage setRejectCode(int rejectCode) {
        this.rejectCode = rejectCode;
        return this;
    }

    public String getConfig() {
        return config;
    }

    public ConfigMessage setConfig(String config) {
        this.config = config;
        return this;
    }

    public String getValue() {
        return value;
    }

    public ConfigMessage setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return String.format("Description : %s Request Type : %d ", module, requestType);
    }
}
