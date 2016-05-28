package io.oddworks.device.request;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import java.util.Locale;

import io.oddworks.device.Oddworks;
import io.oddworks.device.exception.RestServicesNotInitialized;
import io.oddworks.device.metric.OddMetric;
import io.oddworks.device.model.AuthToken;

/**
 * For calling the api and returning raw responses
 */
public class RequestHandler {
    protected static RequestHandler instance;

    /** supported request methods. if you add new ones, you'll probably have to make changes to existing
    RequestHandler methods */
    private enum RequestMethod { GET, POST }
    private static MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static String acceptHeader = "application/json";

    private AuthToken authToken;
    private OkHttpClient okHttpClient = new OkHttpClient();
    private String versionName;
    private HttpUrl baseUrl;
    private String accessToken;
    private String language = Locale.getDefault().getLanguage().toLowerCase();
    private String country = Locale.getDefault().getCountry().toLowerCase();
    private String locale = language + "-" + country;

    /**
     * @param context Application context for fetching resources
     */
    protected RequestHandler(@NonNull Context context) {
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String accessToken = info.metaData.getString("io.oddworks.accessToken");
            String versionName = packageInfo.versionName;

            this.versionName = versionName;

            if (accessToken != null) {
                this.accessToken = accessToken;
            } else {
                throw new RestServicesNotInitialized("io.oddworks.accessToken is required");
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new RestServicesNotInitialized("io.oddworks.accessToken is required");
        }

        this.baseUrl = new HttpUrl.Builder().scheme(Oddworks.API_PROTOCOL).host(Oddworks.API_HOST).addPathSegment(Oddworks.API_VERSION).build();
    }

    /** get an authorized (if AuthToken is set) get request for the endpoint */
    private Request getOddGetRequest(HttpUrl endpoint) {
        return getOddRequest(endpoint, RequestMethod.GET, null, false);
    }

    private Request getOddRequest(HttpUrl endpoint, RequestMethod method, RequestBody body, boolean forceNoAuth) {
        Request.Builder builder = new Request.Builder();

        builder.url(endpoint.toString())
                .addHeader("x-access-token", accessToken)
                .addHeader("x-odd-user-agent", getOddUserAgent())
                .addHeader("accept", acceptHeader)
                .addHeader("accept-language", locale);
        if(!forceNoAuth && authToken != null) {
            builder.addHeader("authorization", authToken.getTokenType() + " " + authToken.getToken());
        }
        if(method == RequestMethod.POST) {
            builder.post(body);
        }
        return builder.build();
    }

    private String getOddUserAgent() {
        String oddUserAgent = "";

        oddUserAgent += "platform[name]=Google";
        oddUserAgent += "&model[name]=" + Build.MANUFACTURER;
        oddUserAgent += "&model[version]=" + Build.MODEL;
        oddUserAgent += "&os[name]=Android";
        oddUserAgent += "&os[version]=" + Build.VERSION.RELEASE;
        oddUserAgent += "&build[version]=" + versionName;

        return oddUserAgent;
    }

    private void enqueueOddCall(Request request, Callback callback) {
        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);
    }

    protected void getConfig(final Callback callback) {
        HttpUrl.Builder builder = withIncluded(baseUrl.newBuilder());
        HttpUrl endpoint = withPath(builder, Oddworks.ENDPOINT_CONFIG).build();

        Request request = getOddRequest(
                endpoint,
                RequestMethod.GET,
                null,
                true);
        enqueueOddCall(request, callback);
    }

    protected void getView(final String id, final Callback callback) {
        String viewPath = String.format(Oddworks.ENDPOINT_VIEW, id);

        HttpUrl.Builder builder = withIncluded(baseUrl.newBuilder());
        HttpUrl endpoint = withPath(builder, viewPath).build();

        Request request = getOddGetRequest(endpoint);
        enqueueOddCall(request, callback);
    }

    protected void getCollectionEntities(String collectionId, Callback callback) {
        String path = String.format(Oddworks.ENDPOINT_COLLECTION_ENTITIES, collectionId);

        HttpUrl endpoint = withPath(baseUrl.newBuilder(), path).build();
        Request request = getOddGetRequest(endpoint);
        enqueueOddCall(request, callback);
    }

    protected void getSearch(String term, int limit, int offset, Callback callback) {
        HttpUrl endpoint = withPath(baseUrl.newBuilder(), Oddworks.ENDPOINT_SEARCH)
                .addQueryParameter(Oddworks.QUERY_PARAM_TERM, term)
                .addQueryParameter(Oddworks.QUERY_PARAM_LIMIT, String.valueOf(limit))
                .addQueryParameter(Oddworks.QUERY_PARAM_OFFSET, String.valueOf(offset))
                .build();
        Request request = getOddGetRequest(endpoint);
        enqueueOddCall(request, callback);
    }

    protected void getCollection(String collectionId, Callback callback) {
        String path = String.format(Oddworks.ENDPOINT_COLLECTION, collectionId);
        HttpUrl.Builder builder = withIncluded(baseUrl.newBuilder());
        HttpUrl endpoint = withPath(builder, path).build();
        Request request = getOddGetRequest(endpoint);
        enqueueOddCall(request, callback);
    }

    protected void postMetric(OddMetric event, Callback callback) {
        HttpUrl endpoint = withPath(baseUrl.newBuilder(), Oddworks.ENDPOINT_EVENTS).build();

        Request request = getOddRequest(endpoint, RequestMethod.POST, RequestBody.create(JSON, event.toJSONObject().toString()), true);
        enqueueOddCall(request, callback);
    }

    protected void getAuthDeviceCode(Callback callback) {
        HttpUrl endpoint = withPath(baseUrl.newBuilder(), Oddworks.ENDPOINT_AUTH_DEVICE_CODE).build();

        Request request = getOddRequest(endpoint, RequestMethod.POST, RequestBody.create(JSON, ""), true);
        enqueueOddCall(request, callback);
    }

    protected void getAuthToken(Callback callback, String deviceCode) {
        HttpUrl endpoint = withPath(baseUrl.newBuilder(), Oddworks.ENDPOINT_AUTH_DEVICE_TOKEN).build();

        RequestBody body = RequestBody.create(JSON,
                "{ \"type\":\"authorized_user\", \"attributes\": {\"device_code\":\"" + deviceCode + "\"}}");
        Request request = getOddRequest(endpoint, RequestMethod.POST, body, true);
        Log.d("getAuthToken", "request body: " + request.body().toString());
        enqueueOddCall(request, callback);
    }

    protected void getVideo(String videoId, Callback callback) {
        String path = String.format(Oddworks.ENDPOINT_VIDEO, videoId);
        HttpUrl.Builder builder = withIncluded(baseUrl.newBuilder());
        HttpUrl endpoint = withPath(builder, path).build();
        Request request = getOddGetRequest(endpoint);
        enqueueOddCall(request, callback);
    }

    protected void getLiveStream(String liveStreamId, Callback callback) {
        String path = String.format(Oddworks.ENDPOINT_LIVE_STREAM, liveStreamId);
        HttpUrl.Builder builder = withIncluded(baseUrl.newBuilder());
        HttpUrl endpoint = withPath(builder, path).build();
        Request request = getOddGetRequest(endpoint);
        enqueueOddCall(request, callback);
    }

    protected void getPromotion(String promotionId, Callback callback, boolean fetchIncluded) {
        String path = String.format(Oddworks.ENDPOINT_PROMOTION, promotionId);
        HttpUrl.Builder builder = null;
        if(fetchIncluded)
            builder = withIncluded(baseUrl.newBuilder());
        else
            builder = baseUrl.newBuilder();
        HttpUrl endpoint = withPath(builder, path).build();
        Request request = getOddGetRequest(endpoint);
        enqueueOddCall(request, callback);
    }

    protected void getExternal(String externalId, Callback callback, boolean fetchIncluded) {
        String path = String.format(Oddworks.ENDPOINT_EXTERNAL, externalId);
        HttpUrl.Builder builder = null;
        if(fetchIncluded)
            builder = withIncluded(baseUrl.newBuilder());
        else
            builder = baseUrl.newBuilder();
        HttpUrl endpoint = withPath(builder, path).build();
        Request request = getOddGetRequest(endpoint);
        enqueueOddCall(request, callback);
    }

    protected void getEvent(String eventId, Callback callback, boolean fetchIncluded) {
        String path = String.format(Oddworks.ENDPOINT_EVENT, eventId);
        HttpUrl.Builder builder = null;
        if(fetchIncluded)
            builder = withIncluded(baseUrl.newBuilder());
        else
            builder = baseUrl.newBuilder();
        HttpUrl endpoint = withPath(builder, path).build();
        Request request = getOddGetRequest(endpoint);
        enqueueOddCall(request, callback);
    }

    protected void getArticle(String articleId, Callback callback, boolean fetchIncluded) {
        String path = String.format(Oddworks.ENDPOINT_ARTICLES, articleId);
        HttpUrl.Builder builder = null;
        if(fetchIncluded)
            builder = withIncluded(baseUrl.newBuilder());
        else
            builder = baseUrl.newBuilder();
        HttpUrl endpoint = withPath(builder, path).build();
        Request request = getOddGetRequest(endpoint);
        enqueueOddCall(request, callback);
    }

    /**
     * @param host sets Oddworks API host
     */
    public void setHost(@NonNull String host) {
        this.baseUrl = baseUrl.newBuilder().host(host).build();
    }

    /**
     * @param port sets Oddworks API port
     */
    public void setPort(@NonNull int port) {
        this.baseUrl = baseUrl.newBuilder().port(port).build();
    }

    /**
     * @param scheme sets Oddworks API scheme
     *               must be http or https
     */
    public void setScheme(@NonNull String scheme) {
        this.baseUrl = baseUrl.newBuilder().scheme(scheme).build();
    }

    /**
     * @param versionName sets application version used in x-odd-user-agent header (build[version])
     */
    public void setVersionName(@NonNull String versionName) {
        this.versionName = versionName;
    }

    /**
     * @param accessToken sets Oddworks API accessToken for x-access-token-header
     */
    public void setAccessToken(@NonNull String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * @param apiVersion sets Oddworks API version number
     */
    public void setApiVersion(@NonNull String apiVersion) {
        this.baseUrl = new HttpUrl.Builder().scheme(baseUrl.scheme()).host(baseUrl.host()).addPathSegment(apiVersion).build();
    }

    /** Set an AuthToken to be used in api calls */
    public void setAuthToken(AuthToken authToken) {
        this.authToken = authToken;
    }

    /** remove auth token. Requests will no longer use auth */
    public void removeAuthToken() {
        this.authToken = null;
    }

    /**
     * @return true if a token has been set, otherwise false
     */
    public boolean isAuthTokenSet() {
        return this.authToken != null;
    }

    /**
     * adds query parameter includ=true to endpoint string.
     * @param endpoint endpoint string can optionally contain query params
     */
    private HttpUrl.Builder withIncluded(HttpUrl.Builder endpoint) {
        return endpoint;
    }

    private HttpUrl.Builder withPath(HttpUrl.Builder endpoint, String path) {
        String[] parts = path.split("/");
        for (String part : parts) {
            endpoint.addPathSegment(part);
        }
        return endpoint;
    }
}
