package io.oddworks.device.model;

import java.util.LinkedHashMap;

public class Config {
    public static final String TAG = Config.class.getSimpleName();
    private final boolean authEnabled;
    private final MetricsConfig metricsConfig;

    private LinkedHashMap<String, String> views;
    private String appMap;

    public Config(LinkedHashMap<String, String> views,
                  boolean authEnabled,
                  MetricsConfig metricsConfig, String appMap) {
        this.views = views;
        this.authEnabled = authEnabled;
        this.metricsConfig = metricsConfig;
        this.appMap = appMap;
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
}
