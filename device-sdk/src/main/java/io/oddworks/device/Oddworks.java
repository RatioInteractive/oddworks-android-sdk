package io.oddworks.device;

public class Oddworks {

    public static final String ENDPOINT_AUTH_DEVICE_CODE = "auth/device/code";
    public static final String ENDPOINT_AUTH_DEVICE_TOKEN = "auth/device/token";
    public static final String ENDPOINT_COLLECTION = "collections/%s";
    public static final String ENDPOINT_VIDEO = "videos/%s";
    public static final String ENDPOINT_LIVE_STREAM = "liveStreams/%s";
    public static final String ENDPOINT_PROMOTION = "promotions/%s";
    public static final String ENDPOINT_EXTERNAL  = "externals/%s";
    public static final String ENDPOINT_EVENT  = "events/%s";
    public static final String ENDPOINT_ARTICLES  = "events/%s";
    public static final String ENDPOINT_COLLECTION_ENTITIES = "collections/%s/relationships/entities";
    public static final String ENDPOINT_CONFIG = "config";
    public static final String ENDPOINT_EVENTS = "events";
    public static final String ENDPOINT_VIEW = "views/%s";
    public static final String ENDPOINT_SEARCH = "search";

    public static final String QUERY_PARAM_INCLUDE = "include";
    public static final String QUERY_PARAM_LIMIT = "limit";
    public static final String QUERY_PARAM_OFFSET = "offset";
    public static final String QUERY_PARAM_TERM = "q";


    public static final String API_HOST = "device.oddworks.io";
    public static final String API_HOST_STAGING = "device-staging.oddworks.io";
    public static final String API_HOST_DEMO = "beta.oddworks.io";

    public static final String API_PROTOCOL = "https";

    public static final String API_VERSION = "v2";

    public static final String ODD_NETWORKS_ACCESS_TOKEN_KEY = "io.oddworks.accessToken";

    private Oddworks() {
        throw new AssertionError();
    }
}
