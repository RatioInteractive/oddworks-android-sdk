package io.oddworks.device.request;

import android.content.Context;
import android.support.annotation.NonNull;

import net.danlew.android.joda.JodaTimeAndroid;

import io.oddworks.device.model.AuthToken;


/**
 * Class to initialize services in the io.oddworks.device.request package
 */
public class RestServiceProvider {
    private static RestServiceProvider instance;

    private ApiCaller apiCaller;
    private AuthenticationService authenticationService;
    private CachingApiCaller cachingApiCaller;

    private RestServiceProvider(@NonNull Context context) {
        RequestHandler.instance = new RequestHandler(context);
        ApiCaller.instance = new ApiCaller(RequestHandler.instance, OddParser.getInstance());
        this.cachingApiCaller =
                new CachingApiCaller(RequestHandler.instance, OddParser.getInstance(), new EntityCache());
        this.apiCaller = ApiCaller.instance;
        AuthenticationService.instance = new AuthenticationService(ApiCaller.instance, context);
        this.authenticationService = AuthenticationService.instance;

        JodaTimeAndroid.init(context);
    }

    /**
     * @param context Application context for accessing resources */
    public static void init(@NonNull Context context) {
        instance = new RestServiceProvider(context);
    }

    /**
     * @return a RestServiceProvider instance if init has already been called. Otherwise returns null.
     */
    public static RestServiceProvider getInstance() {
        return RestServiceProvider.instance;
    }

    public static RequestHandler getRequestHandler() { return RequestHandler.instance; }

    public ApiCaller getApiCaller() {
        return this.apiCaller;
    }

    public CachingApiCaller getCachingApiCaller() {
        return this.cachingApiCaller;
    }

    public AuthenticationService getAuthenticationService() {
        return this.authenticationService;
    }

    public void setAuthToken(AuthToken authToken) {
        RequestHandler.instance.setAuthToken(authToken);
    }

    public void removeAuthToken() {
        RequestHandler.instance.removeAuthToken();
    }

    public boolean isAuthTokenSet() {
        return RequestHandler.instance.isAuthTokenSet();
    }
}