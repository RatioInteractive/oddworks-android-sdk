package io.oddworks.device.model;

import java.util.LinkedHashMap;

public class Config {
    public static final String TAG = Config.class.getSimpleName();
    private final boolean authEnabled;
    private final MetricsConfig metricsConfig;

    private LinkedHashMap<String, String> views;
    private String appMap;
    private String styles;
    private String channels;
    private String receiverId;
    private String tveConfig;
    private String schedulesUri;

    public Config(LinkedHashMap<String, String> views,
                  boolean authEnabled,
                  MetricsConfig metricsConfig, String appMap, String styles, String channels, String receiverId, String tveConfig, String schedulesUri) {
        this.views = views;
        this.authEnabled = authEnabled;
        this.metricsConfig = metricsConfig;
        this.appMap = appMap;
        this.styles = styles;
        this.channels = channels;
        this.receiverId = receiverId;
        this.tveConfig = tveConfig;
        this.schedulesUri = schedulesUri;
    }


    public boolean isAuthEnabled() {
        return authEnabled;
    }

    public MetricsConfig getMetricsConfig() {
        return metricsConfig;
    }

    public LinkedHashMap<String, String> getViews() {
        return views;
    }

    public String getAppMap() {
        return appMap;
    }

    public void setAppMap(String appMap) {
        this.appMap = appMap;
    }

    public String getStyles() {
        return styles;
    }

    public void setStyles(String styles) {
        this.styles = styles;
    }

    public void setChannels(String channels) {
        this.channels = channels;
    }

    public String getChannels() {
        return channels;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getTVEConfig() {
        return tveConfig;
    }

    public void setTVEConfig(String tveConfig) {
        this.tveConfig = tveConfig;
    }

    public String getSchedulesUri() {
        return schedulesUri;
    }

    public void setSchedulesUri(String schedulesUri) {
        this.schedulesUri = schedulesUri;
    }
}
