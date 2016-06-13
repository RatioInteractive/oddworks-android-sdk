package io.oddworks.device.request;

import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import io.oddworks.device.exception.BadResponseCodeException;
import io.oddworks.device.exception.OddAuthTokenUserMismatch;
import io.oddworks.device.exception.OddParseException;
import io.oddworks.device.metric.OddMetric;
import io.oddworks.device.model.AuthToken;
import io.oddworks.device.model.Config;
import io.oddworks.device.model.DeviceCodeResponse;
import io.oddworks.device.model.Media;
import io.oddworks.device.model.OddCollection;
import io.oddworks.device.model.OddObject;
import io.oddworks.device.model.OddView;

/**
 * Class of methods for handling api calls. Is instantiated using the ReqeustServicesInitializer and afterward can be
 * accessed through the instance field
 */
public class ApiCaller {
    public static final String AUTH_TOKEN_MISMATCH = "DeviceID accessToken/authorizationToken mismatch";
    protected static ApiCaller instance;
    public static final int RESPONSE_OK = 200;
    public static final int RESPONSE_CREATED = 201;
    public static final int RESPONSE_NOT_FOUND = 404;


    public static final int MS_IN_SECONDS = 1000;
    public static String TAG = ApiCaller.class.getSimpleName();
    private final RequestHandler requestHandler;
    private final OddParser parser;

    protected ApiCaller(RequestHandler requestHandler, OddParser parser){
        this.requestHandler = requestHandler;
        this.parser = parser;
    }

    /** gets Config
     *
     * @param cb OddCallback **/
    public void getConfig(final OddCallback<Config> cb) {
        getConfig(cb, null);
    }

    /** gets Config
     *
     * @param cb OddCallback
     * @param params Map of params to be used as query params.  Can be null **/
    public void getConfig(final OddCallback<Config> cb, Map<String,List<String>> params) {
        Callback requestCallback = getRequestCallback(cb, new ParseCall<Config>() {
            @Override
            public Config parse(String responseBody) {
                return parser.parseConfig(responseBody);
            }
        });
        requestHandler.getConfig(requestCallback, params);
    }

    /** gets OddView
     *
     * @param id view id
     * @param cb OddCallback
     * @param fetchIncluded if true will use "include" params
     * @param params Map of params to be used as query params.  Can be null
     * **/
    public void getView(final String id, final OddCallback<OddView> cb, boolean fetchIncluded, Map<String,List<String>> params) {
        Callback requestCallback = getRequestCallback(cb, new ParseCall<OddView>() {
            @Override
            public OddView parse(String responseBody) {
                return parser.parseViewResponse(responseBody);
            }
        });
        requestHandler.getView(id, requestCallback, fetchIncluded, params);
    }

    public void getCollectionEntities(final OddCollection col, final OddCallback<List<OddObject>> cb) {
        Callback requestCallback = getRequestCallback(cb, new ParseCall<List<OddObject>>() {
            @Override
            public List<OddObject> parse(String responseBody) {
                return parser.parseEntityList(responseBody);
            }
        });
        requestHandler.getCollectionEntities(col.getId(), requestCallback);
    }

    /** gets OddCollection and all entities within
     *
     * @param collectionId
     * @param cb OddCallback
     * @param fetchIncluded if true will use "include" params
     * @param params Map of params to be used as query params.  Can be null
     * **/
    public void getCollection(String collectionId, final OddCallback<OddCollection> cb, boolean fetchIncluded, Map<String,List<String>> params) {
        Callback requestCallback = getRequestCallback(cb, new ParseCall<OddCollection>() {
            @Override
            public OddCollection parse(String responseBody) {
                return parser.parseCollectionResponse(responseBody);
            }
        });
        requestHandler.getCollection(collectionId, requestCallback, fetchIncluded, params);
    }

    /** gets video with all related entities
     *
     * @param collectionId
     * @param isLiveStream true if this Media is a liveStream object in the api's catalog otherwise false.
     *                          If Media#isLive() returns true then then this should be true.
     * @param cb OddCallback
     * @param fetchIncluded if true will use "include" params
     * @param params Map of params to be used as query params.  Can be null
     * **/
    public void getMedia(String collectionId, boolean isLiveStream, final OddCallback<Media> cb, boolean fetchIncluded, Map<String,List<String>> params) {
        Callback requestCallback = getRequestCallback(cb, new ParseCall<Media>() {
            @Override
            public Media parse(String responseBody) {
                return parser.parseMediaResponse(responseBody);
            }
        });
        if(isLiveStream)
            requestHandler.getLiveStream(collectionId, requestCallback, fetchIncluded, params);
        else
            requestHandler.getVideo(collectionId, requestCallback, fetchIncluded, params);
    }

    public void getSearch(final String term, final int limit, final int offset, final OddCallback<List<OddObject>> cb) {
        Callback requestCallback = getRequestCallback(cb, new ParseCall<List<OddObject>>() {

            @Override
            public List<OddObject> parse(String responseBody) throws JSONException {
                return parser.parseSearch(responseBody);
            }
        });

        requestHandler.getSearch(term, limit, offset, requestCallback);
    }

    public void postMetric(final OddMetric event, final OddCallback<OddMetric> cb) {
        Callback requestCallback = getRequestCallback(cb, new ParseCall<OddMetric>() {
            @Override
            public OddMetric parse(String responseBody) throws JSONException {
                return event;
            }
        });

        requestHandler.postMetric(event, requestCallback);
    }

    protected void getPollingAuthenticator(final OddCallback<PollingAuthenticator> cb) {
        final ApiCaller thisApiCaller = this;
        ParseCall<PollingAuthenticator> parseAuthenticator = new ParseCall<PollingAuthenticator>() {
            @Override
            public PollingAuthenticator parse(String responseBody) throws JSONException {
                DeviceCodeResponse parsedResponse = parser.parseDeviceCodeResponse(responseBody);

                return new PollingAuthenticator(parsedResponse.getDeviceCode(),
                        parsedResponse.getUserCode(),
                        parsedResponse.getVerificationUrl(),
                        parsedResponse.getExpirationDate(),
                        parsedResponse.getIntervalSeconds() * MS_IN_SECONDS,
                        thisApiCaller
                );
            }
        };
        Callback requestCallback = getRequestCallback(cb, parseAuthenticator);
        requestHandler.getAuthDeviceCode(requestCallback);
    }

    protected void tryGetAuthToken(final String deviceCode, final OddCallback<AuthToken> cb){
        Log.d(this.getClass().getSimpleName(), "start tryGetAuthToken");
        Callback requestCallback = getRequestCallback(cb, new ParseCall<AuthToken>() {
            @Override
            public AuthToken parse(String responseBody) throws JSONException {
                return parser.parseAuthToken(responseBody);
            }
        });
        requestHandler.getAuthToken(requestCallback, deviceCode);
    }

    private <T> Callback getRequestCallback(final OddCallback<T> cb, final ParseCall<T> parseCall) {
        return new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e("ResponseCb onFailure", "Failed", e);

                cb.onFailure(e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.d("ResponseCb onResponse", "code :"
                        + response.code() + " responseBody: " + response);
                if (response.isSuccessful()) {
                    T obj = null;
                    Exception exceptionCaught = null;
                    String bodyString = null;
                    try {
                        obj = parseCall.parse(response.body().string());
                    } catch (Exception e) {
                        exceptionCaught = e;
                        bodyString = tryGetResponseBody(response);
                    }

                    if(exceptionCaught != null) {
                        cb.onFailure(new OddParseException(
                                "Parse for response body failed: " + bodyString, exceptionCaught));
                    } else {
                        cb.onSuccess(obj);
                    }

                } else {
                    try {
                        if (response.code() == 400 &&
                                parser.parseErrorMessage(response.body().string()).equals(AUTH_TOKEN_MISMATCH)) {
                            cb.onFailure(new OddAuthTokenUserMismatch(400));
                            return;
                        }
                    } catch (Exception e) {
                        //do nothing. This was not an accessToken authorization token mismatch.
                    }
                    cb.onFailure(new BadResponseCodeException(response.code()));
                }
            }

            private String tryGetResponseBody(Response response) {
                String bodyString;
                try {
                    bodyString = response.body().string();
                } catch(Exception e) {
                    bodyString = "body could not be captured in this error message." +
                            " This is not the cause of the exception and can be ignored";;
                }
                return bodyString;
            }
        };
    }

    /**
     * Set the Oddworks API accessToken for x-access-token header
     *
     * @param accessToken
     */
    public void setAccessToken(String accessToken) {
        requestHandler.setAccessToken(accessToken);
    }

    /**
     * Set the Oddworks API host in the RequestHandler instance
     *
     * @param host
     */
    public void setHost(String host) {
        requestHandler.setHost(host);
    }

    /**
     * Set the Oddworks API port in the RequestHandler instance
     *
     * @param port
     */
    public void setPort(int port) {
        requestHandler.setPort(port);
    }

    /**
     * Set Oddworks API scheme in the RequestHandler instance
     *
     * @param scheme must be http or https
     */
    public void setScheme(String scheme) {
        requestHandler.setScheme(scheme);
    }

    /**
     * Set the Oddworks API version
     * @param apiVersion
     */
    public void setApiVersion(String apiVersion) {
        requestHandler.setApiVersion(apiVersion);
    }

    /**
     * Set the versionName for Oddworks x-odd-user-agent header parameter: build[version]
     *
     * @param versionName
     */
    public void setVersionName(String versionName) {
        requestHandler.setVersionName(versionName);
    }

    /** Deprecated. Use RestServiceProvider#setAuthToken instead
     *  Set an AuthToken to be used in api calls */
    @Deprecated
    public void setAuthToken(AuthToken authToken) {
        requestHandler.setAuthToken(authToken);
    }

    /** Deprecated. Use RestServiceProvider#setRemoveAuthToken instead
     * remove auth token. Requests will no longer use auth */
    @Deprecated
    public void removeAuthToken() {
        requestHandler.removeAuthToken();
    }

    /**
     * Deprecated. Use RestServiceProvider#isAuthTokenSet instead
     * @return true if a token has been set, otherwise false
     */
    @Deprecated
    public boolean isAuthTokenSet() {
        return requestHandler.isAuthTokenSet();
    }

    private interface ParseCall<T> {
        T parse(String responseBody) throws JSONException;
    }
}
