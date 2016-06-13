package io.oddworks.device.request;

import android.support.annotation.NonNull;
import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.oddworks.device.exception.BadResponseCodeException;
import io.oddworks.device.exception.OddAuthTokenUserMismatch;
import io.oddworks.device.exception.OddParseException;
import io.oddworks.device.model.Article;
import io.oddworks.device.model.Event;
import io.oddworks.device.model.External;
import io.oddworks.device.model.Identifier;
import io.oddworks.device.model.Media;
import io.oddworks.device.model.OddCollection;
import io.oddworks.device.model.OddObject;
import io.oddworks.device.model.OddView;
import io.oddworks.device.model.Promotion;
import io.oddworks.device.model.Relationship;

/**
 * This class contains methods for fetching data from the API Server with automatic caching. Http Cache-Control headers
 * returned by the API are respected. Whenever possible, this should be used instead of the ApiCaller.
 *
 * @author Dan Pallas
 * @since 1.4 on 03/02/2016
 */
public class CachingApiCaller {
    public static final String AUTH_TOKEN_MISMATCH = "DeviceID accessToken/authorizationToken mismatch";
    private final EntityCache cache;
    private final OddParser parser;
    private final RequestHandler requestHandler;

    protected CachingApiCaller(RequestHandler requestHandler,
                               OddParser parser,
                               EntityCache cache) {
        this.cache = cache;
        this.parser = parser;
        this.requestHandler = requestHandler;
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param forceFetchFromApi if true, do not attempt to get from cache. Get directly from api
     * @param fetchIncluded if true will use "include" params
     * @param params Map of params to be used as query params.  Can be null
     */
    public void getPromotion(@NonNull String id,
                             @NonNull OddCallback<Promotion> cb,
                             boolean forceFetchFromApi,
                             boolean fetchIncluded,
                             Map<String,List<String>> params) {

        try {
            Promotion stored = (Promotion)cache.getObject(id);
            if(!forceFetchFromApi && stored != null && objectHasAllIncluded(stored, RequestHandler.getIncludedParams(params))) {
                cb.onSuccess(stored);
            } else {
                getPromotionFromApi(id, cb, fetchIncluded, params);
            }
        } catch (Exception e) {
            cb.onFailure(e);
        }
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param forceFetchFromApi if true, do not attempt to get from cache. Get directly from api
     */
    public void getPromotion(@NonNull String id, @NonNull OddCallback<Promotion> cb, boolean forceFetchFromApi) {
        getPromotion(id, cb, forceFetchFromApi, false, null);
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param forceFetchFromApi if true, do not attempt to get from cache. Get directly from api
     * @param fetchIncluded if true will use "include" params
     * @param params Map of params to be used as query params.  Can be null
     */
    public void getPromotionWithAllRelated(@NonNull final String id,
                                         @NonNull final OddCallback<ObjectWithRelated<Promotion>> cb,
                                         boolean forceFetchFromApi,
                                         boolean fetchIncluded,
                                         Map<String,List<String>> params) {
        if(forceFetchFromApi || !completeGetAllRelatedFromCache(id, cb))
            getPromotionFromApi(id, new GatherAllRelatedCb<Promotion>(cb), fetchIncluded, params);
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param forceFetchFromApi if true, do not attempt to get from cache. Get directly from api
     * @param fetchIncluded if true will use "include" params
     * @param params Map of params to be used as query params.  Can be null
     */
    public void getPromotionWithRelationship(@NonNull final String id,
                                           @NonNull final OddCallback<ObjectWithRelated<Promotion>> cb,
                                           @NonNull final String relationshipName,
                                           boolean forceFetchFromApi,
                                           boolean fetchIncluded,
                                           Map<String,List<String>> params) {
        if(forceFetchFromApi || !completeGetRelationshipFromCache(id, relationshipName, cb))
            getPromotionFromApi(id, new GatherRelationshipCb<Promotion>(cb, relationshipName), fetchIncluded, params);
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param fetchIncluded if true will use "include" params
     * @param params Map of params to be used as query params.  Can be null
     */
    private void getPromotionFromApi(String id, final OddCallback<Promotion> cb, boolean fetchIncluded,
                                     Map<String,List<String>> params) {
        Callback requestCallback = new RequestCallback<>(cb, new ParseCall<Promotion>() {
            @Override
            public Promotion parse(String responseBody) {
                return parser.parsePromotionResponse(responseBody);
            }
        });
        requestHandler.getPromotion(id, requestCallback, fetchIncluded, params);
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param forceFetchFromApi if true, do not attempt to get from cache. Get directly from api
     * @param fetchIncluded if true will use "include" params
     * @param params Map of params to be used as query params.  Can be null
     */
    public void getExternal(@NonNull String id, @NonNull OddCallback<External> cb, boolean forceFetchFromApi,
                            boolean fetchIncluded,
                            Map<String,List<String>> params) {
        try {
            External stored = (External)cache.getObject(id);
            if(!forceFetchFromApi && stored != null && objectHasAllIncluded(stored, RequestHandler.getIncludedParams(params))) {
                cb.onSuccess(stored);
            } else {
                getExternalFromApi(id, cb, fetchIncluded, params);
            }
        } catch (Exception e) {
            cb.onFailure(e);
        }
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param forceFetchFromApi if true, do not attempt to get from cache. Get directly from api
     */
    public void getExternal(@NonNull String id, @NonNull OddCallback<External> cb, boolean forceFetchFromApi) {
        getExternal(id, cb, forceFetchFromApi, false, null);
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param forceFetchFromApi if true, do not attempt to get from cache. Get directly from api
     * @param fetchIncluded if true will use "include" params
     * @param params Map of params to be used as query params.  Can be null
     */
    public void getExternalWithAllRelated(@NonNull final String id,
                                          @NonNull final OddCallback<ObjectWithRelated<External>> cb,
                                          boolean forceFetchFromApi,
                                          boolean fetchIncluded,
                                          Map<String,List<String>> params) {
        if(forceFetchFromApi || !completeGetAllRelatedFromCache(id, cb))
            getExternalFromApi(id, new GatherAllRelatedCb<External>(cb), fetchIncluded, params);
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param forceFetchFromApi if true, do not attempt to get from cache. Get directly from api
     * @param fetchIncluded if true will use "include" params
     * @param params Map of params to be used as query params.  Can be null
     */
    public void getExternalWithRelationship(@NonNull final String id,
                                            @NonNull final OddCallback<ObjectWithRelated<External>> cb,
                                            @NonNull final String relationshipName,
                                            boolean forceFetchFromApi,
                                            boolean fetchIncluded,
                                            Map<String,List<String>> params) {
        if(forceFetchFromApi || !completeGetRelationshipFromCache(id, relationshipName, cb))
            getExternalFromApi(id, new GatherRelationshipCb<>(cb, relationshipName), fetchIncluded, params);
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param fetchIncluded if true will use "include" params
     * @param params Map of params to be used as query params.  Can be null
     */
    private void getExternalFromApi(String id, OddCallback<External> cb, boolean fetchIncluded,
                                    Map<String,List<String>> params) {
        Callback requestCallback = new RequestCallback<>(cb, new ParseCall<External>() {
            @Override
            public External parse(String responseBody) {
                return parser.parseExternalResponse(responseBody);
            }
        });
        requestHandler.getExternal(id, requestCallback, fetchIncluded, params);
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param forceFetchFromApi if true, do not attempt to get from cache. Get directly from api
     * @param fetchIncluded if true will use "include" params
     * @param params Map of params to be used as query params.  Can be null
     */
    public void getCollection(@NonNull String id,
                              @NonNull OddCallback<OddCollection> cb,
                              boolean forceFetchFromApi,
                              boolean fetchIncluded,
                              Map<String,List<String>> params) {
        try {
            OddCollection stored = (OddCollection) cache.getObject(id);
            if (!forceFetchFromApi && stored != null && objectHasAllIncluded(stored, RequestHandler.getIncludedParams(params))) {
                cb.onSuccess(stored);
            } else {
                getCollectionFromApi(id, cb, fetchIncluded, params);
            }
        } catch (Exception e) {
            cb.onFailure(e);
        }
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param forceFetchFromApi if true, do not attempt to get from cache. Get directly from api
     */
    public void getCollection(@NonNull String id, @NonNull OddCallback<OddCollection> cb, boolean forceFetchFromApi) {
        getCollection(id, cb, forceFetchFromApi, false, null);
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param forceFetchFromApi if true, do not attempt to get from cache. Get directly from api
     * @param fetchIncluded if true will use "include" params
     * @param params Map of params to be used as query params.  Can be null
     */
    public void getCollectionWithAllRelated(@NonNull final String id,
                                            @NonNull final OddCallback<ObjectWithRelated<OddCollection>> cb,
                                            boolean forceFetchFromApi,
                                            boolean fetchIncluded,
                                            Map<String,List<String>> params) {
        if(forceFetchFromApi || !completeGetAllRelatedFromCache(id, cb))
            getCollectionFromApi(id, new GatherAllRelatedCb<OddCollection>(cb), fetchIncluded, params);
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param forceFetchFromApi if true, do not attempt to get from cache. Get directly from api
     * @param fetchIncluded if true will use "include" params
     * @param params Map of params to be used as query params.  Can be null
     */
    public void getCollectionWithRelationship(@NonNull final String id,
                                              @NonNull final OddCallback<ObjectWithRelated<OddCollection>> cb,
                                              @NonNull final String relationshipName,
                                              boolean forceFetchFromApi,
                                              boolean fetchIncluded,
                                              Map<String,List<String>> params) {
        if(forceFetchFromApi || !completeGetRelationshipFromCache(id, relationshipName, cb))
            getCollectionFromApi(id, new GatherRelationshipCb<>(cb, relationshipName), fetchIncluded, params);
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param fetchIncluded if true will use "include" params
     * @param params Map of params to be used as query params.  Can be null
     */
    private void getCollectionFromApi(String id, OddCallback<OddCollection> cb, boolean fetchIncluded,
                                      Map<String,List<String>> params) {
        Callback requestCallback = new RequestCallback<>(cb, new ParseCall<OddCollection>() {
            @Override
            public OddCollection parse(String responseBody) {
                return parser.parseCollectionResponse(responseBody);
            }
        });
        requestHandler.getCollection(id, requestCallback, fetchIncluded, params);
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param forceFetchFromApi if true, do not attempt to get from cache. Get directly from api
     * @param fetchIncluded if true will use "include" params
     * @param params Map of params to be used as query params.  Can be null
     */
    public void getEvent(@NonNull String id,
                         @NonNull OddCallback<Event> cb,
                         boolean forceFetchFromApi,
                         boolean fetchIncluded,
                         Map<String,List<String>> params) {
       try {
            Event stored = (Event)cache.getObject(id);
            if(!forceFetchFromApi && stored != null && objectHasAllIncluded(stored, RequestHandler.getIncludedParams(params))) {
                cb.onSuccess(stored);
            } else {
                getEventFromApi(id, cb, fetchIncluded, params);
            }
        } catch (Exception e) {
            cb.onFailure(e);
        }
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param forceFetchFromApi if true, do not attempt to get from cache. Get directly from api
     */
    public void getEvent(@NonNull String id, @NonNull OddCallback<Event> cb, boolean forceFetchFromApi) {
        getEvent(id, cb, forceFetchFromApi, false, null);
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param forceFetchFromApi if true, do not attempt to get from cache. Get directly from api
     * @param fetchIncluded if true will use "include" params
     * @param params Map of params to be used as query params.  Can be null
     */
    public void getEventWithAllRelated(@NonNull final String id,
                                       @NonNull final OddCallback<ObjectWithRelated<Event>> cb,
                                       boolean forceFetchFromApi,
                                       boolean fetchIncluded,
                                       Map<String,List<String>> params) {
        if(forceFetchFromApi || !completeGetAllRelatedFromCache(id, cb))
            getEventFromApi(id, new GatherAllRelatedCb<Event>(cb), fetchIncluded, params);
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param relationshipName
     * @param forceFetchFromApi if true, do not attempt to get from cache. Get directly from api
     * @param fetchIncluded if true will use "include" params
     * @param params Map of params to be used as query params.  Can be null
     */
    public void getEventWithRelationship(@NonNull final String id,
                                         @NonNull final OddCallback<ObjectWithRelated<Event>> cb,
                                         @NonNull final String relationshipName,
                                         boolean forceFetchFromApi,
                                         boolean fetchIncluded,
                                         Map<String,List<String>> params) {
        if(forceFetchFromApi || !completeGetRelationshipFromCache(id, relationshipName, cb))
            getEventFromApi(id, new GatherRelationshipCb<>(cb, relationshipName), fetchIncluded, params);
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param fetchIncluded if true will use "include" params
     * @param params Map of params to be used as query params.  Can be null
     */
    private void getEventFromApi(String id, OddCallback<Event> cb, boolean fetchIncluded, Map<String,List<String>> params) {
        Callback requestCallback = new RequestCallback<>(cb, new ParseCall<Event>() {
            @Override
            public Event parse(String responseBody) {
                return parser.parseEventResponse(responseBody);
            }
        });
        requestHandler.getEvent(id, requestCallback, fetchIncluded, params);
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param forceFetchFromApi if true, do not attempt to get from cache. Get directly from api
     * @param fetchIncluded if true will use "include" params
     * @param params Map of params to be used as query params.  Can be null
     * @param isLiveStream true if this Media is a liveStream object in the api's catalog otherwise false.
     *                      If Media#isLive() returns true then then this should be true.
     */
    public void getMedia(@NonNull String id,
                         @NonNull OddCallback<Media> cb,
                         boolean isLiveStream,
                         boolean forceFetchFromApi,
                         boolean fetchIncluded,
                         Map<String,List<String>> params) {

        try {
            Media stored = (Media)cache.getObject(id);
            if(!forceFetchFromApi && stored != null && objectHasAllIncluded(stored, RequestHandler.getIncludedParams(params))) {
                cb.onSuccess(stored);
            } else {
                getMediaFromApi(id, cb, isLiveStream, fetchIncluded, params);
            }
        } catch (Exception e) {
            cb.onFailure(e);
        }
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param forceFetchFromApi if true, do not attempt to get from cache. Get directly from api
     * @param isLiveStream true if this Media is a liveStream object in the api's catalog otherwise false.
     *                      If Media#isLive() returns true then then this should be true.
     */
    public void getMedia(@NonNull String id, @NonNull OddCallback<Media> cb, boolean isLiveStream, boolean forceFetchFromApi) {
        getMedia(id, cb, isLiveStream, forceFetchFromApi, false, null);
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param forceFetchFromApi if true, do not attempt to get from cache. Get directly from api
     * @param params Map of params to be used as query params.  Can be null
     * @param isLiveStream true if this Media is a liveStream object in the api's catalog otherwise false.
     *                      If Media#isLive() returns true then then this should be true.
     */
    public void getMediaWithAllRelated(@NonNull final String id,
                                       boolean isLiveStream,
                                       @NonNull final OddCallback<ObjectWithRelated<Media>> cb,
                                       boolean forceFetchFromApi,
                                       Map<String,List<String>> params) {
        if(forceFetchFromApi || !completeGetAllRelatedFromCache(id, cb))
            getMediaFromApi(id, new GatherAllRelatedCb<>(cb), isLiveStream, true, params);
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param relationshipName
     * @param forceFetchFromApi if true, do not attempt to get from cache. Get directly from api
     * @param params Map of params to be used as query params.  Can be null
     * @param isLiveStream true if this Media is a liveStream object in the api's catalog otherwise false.
     *                      If Media#isLive() returns true then then this should be true.
     */
    public void getMediaWithRelationship(@NonNull final String id,
                                                 @NonNull final OddCallback<ObjectWithRelated<Media>> cb,
                                                 boolean isLiveStream,
                                                 @NonNull final String relationshipName,
                                                 boolean forceFetchFromApi,
                                                 Map<String,List<String>> params) {
        if(forceFetchFromApi || !completeGetRelationshipFromCache(id, relationshipName, cb))
            getMediaFromApi(id, new GatherRelationshipCb<>(cb, relationshipName), isLiveStream, true, params);
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param fetchIncluded if true will use "include" params
     * @param params Map of params to be used as query params.  Can be null
     * @param isLiveStream true if this Media is a liveStream object in the api's catalog otherwise false.
     *                      If Media#isLive() returns true then then this should be true.
     */
    private void getMediaFromApi(String id, OddCallback<Media> cb, boolean isLiveStream,  boolean fetchIncluded, Map<String,List<String>> params) {
        Callback requestCallback = new RequestCallback<>(cb, new ParseCall<Media>() {
            @Override
            public Media parse(String responseBody) {
                return parser.parseMediaResponse(responseBody);
            }
        });
        if(isLiveStream)
            requestHandler.getLiveStream(id, requestCallback, fetchIncluded, params);
        else
            requestHandler.getVideo(id, requestCallback, fetchIncluded, params);
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param forceFetchFromApi if true, do not attempt to get from cache. Get directly from api
     * @param fetchIncluded if true will use "include" params
     * @param params Map of params to be used as query params.  Can be null
     */
    public void getView(@NonNull String id,
                        @NonNull OddCallback<OddView> cb,
                        boolean forceFetchFromApi,
                        boolean fetchIncluded,
                        Map<String,List<String>> params) {
        try {
            OddView stored = (OddView)cache.getObject(id);
            if(!forceFetchFromApi && stored != null && objectHasAllIncluded(stored, RequestHandler.getIncludedParams(params))) {
                cb.onSuccess(stored);
            } else {
                getViewFromApi(id, cb, fetchIncluded, params);
            }
        } catch (Exception e) {
            cb.onFailure(e);
        }
    }

    public void getView(@NonNull String id, @NonNull OddCallback<OddView> cb, boolean forceFetchFromApi) {
        getView(id, cb, forceFetchFromApi, false, null);
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param forceFetchFromApi if true, do not attempt to get from cache. Get directly from api
     * @param params Map of params to be used as query params.  Can be null
     */
    public void getViewWithAllRelated(@NonNull final String id,
                                      @NonNull final OddCallback<ObjectWithRelated<OddView>> cb,
                                      boolean forceFetchFromApi,
                                      Map<String,List<String>> params) {
        if(forceFetchFromApi || !completeGetAllRelatedFromCache(id, cb))
            getViewFromApi(id, new GatherAllRelatedCb<>(cb), true, params);
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param relationshipName
     * @param forceFetchFromApi if true, do not attempt to get from cache. Get directly from api
     * @param params Map of params to be used as query params.  Can be null
     */
    public void getViewWithRelationship(@NonNull final String id,
                                        @NonNull final OddCallback<ObjectWithRelated<OddView>> cb,
                                        @NonNull final String relationshipName,
                                        boolean forceFetchFromApi,
                                        Map<String,List<String>> params) {
        if(forceFetchFromApi || !completeGetRelationshipFromCache(id, relationshipName, cb))
            getViewFromApi(id, new GatherRelationshipCb<>(cb, relationshipName), true, params);
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param fetchIncluded if true will use "include" params
     * @param params Map of params to be used as query params.  Can be null
     */
    private void getViewFromApi(String id, OddCallback<OddView> cb, boolean fetchIncluded, Map<String,List<String>> params) {
        Callback requestCallback = new RequestCallback<>(cb, new ParseCall<OddView>() {
            @Override
            public OddView parse(String responseBody) {
                return parser.parseViewResponse(responseBody);
            }
        });
        requestHandler.getView(id, requestCallback, fetchIncluded, params);
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param forceFetchFromApi if true, do not attempt to get from cache. Get directly from api
     * @param fetchIncluded if true will use "include" params
     * @param params Map of params to be used as query params.  Can be null
     */
    public void getArticle(@NonNull String id,
                           @NonNull OddCallback<Article> cb,
                           boolean forceFetchFromApi,
                           boolean fetchIncluded,
                           Map<String,List<String>> params) {
        try {
            Article stored = (Article)cache.getObject(id);
            if(!forceFetchFromApi && stored != null && objectHasAllIncluded(stored, RequestHandler.getIncludedParams(params))) {
                cb.onSuccess(stored);
            } else {
                getArticleFromApi(id, cb, fetchIncluded, params);
            }
        } catch (Exception e) {
            cb.onFailure(e);
        }
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param forceFetchFromApi if true, do not attempt to get from cache. Get directly from api
     */
    public void getArticle(@NonNull String id, @NonNull OddCallback<Article> cb, boolean forceFetchFromApi) {
        getArticle(id, cb, forceFetchFromApi, false, null);
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param forceFetchFromApi if true, do not attempt to get from cache. Get directly from api
     * @param params Map of params to be used as query params.  Can be null
     */
    public void getArticleWithAllRelated(@NonNull final String id,
                                         @NonNull final OddCallback<ObjectWithRelated<Article>> cb,
                                         boolean forceFetchFromApi,
                                         Map<String,List<String>> params) {
        if(forceFetchFromApi || !completeGetAllRelatedFromCache(id, cb))
            getArticleFromApi(id, new GatherAllRelatedCb<>(cb), true, params);
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param relationshipName
     * @param forceFetchFromApi if true, do not attempt to get from cache. Get directly from api
     * @param params Map of params to be used as query params.  Can be null
     */
    public void getArticleWithRelationship(@NonNull final String id,
                                           @NonNull final OddCallback<ObjectWithRelated<Article>> cb,
                                           @NonNull final String relationshipName,
                                           boolean forceFetchFromApi,
                                           Map<String,List<String>> params) {
        if(forceFetchFromApi || !completeGetRelationshipFromCache(id, relationshipName, cb))
            getArticleFromApi(id, new GatherRelationshipCb<>(cb, relationshipName), true, params);
    }

    /**
     * @param id
     * @param cb OddCallback
     * @param fetchIncluded if true will use "include" params
     * @param params Map of params to be used as query params.  Can be null
     */
    private void getArticleFromApi(String id, OddCallback<Article> cb, boolean fetchIncluded, Map<String,List<String>> params) {
        Callback requestCallback = new RequestCallback<>(cb, new ParseCall<Article>() {
            @Override
            public Article parse(String responseBody) {
                return parser.parseArticleResponse(responseBody);
            }
        });
        requestHandler.getArticle(id, requestCallback, fetchIncluded, params);
    }

    /** attempt to complete callback with data from cache
     * @return true if all data was in the cache so the callback was completed, otherwise false */
    private <T extends OddObject> boolean completeGetAllRelatedFromCache (
            @NonNull String id, @NonNull OddCallback<ObjectWithRelated<T>> cb) {

        try {
            T stored = (T)cache.getObject(id);
            if (stored != null ) {
                List<OddObject> related = cache.getRelatedObjectsOrNull(stored);
                if (related != null) {
                    cb.onSuccess(new ObjectWithRelated<>(stored, related));
                    return true;
                }
            }
        } catch (Exception e) {
            cb.onFailure(e);
        }
        return false;
    }

    /** attempt to complete callback with data from cache
     * @return true if all data was in the cache so the callback was completed, otherwise false */
    private <T extends OddObject> boolean completeGetRelationshipFromCache (
            @NonNull String id, @NonNull String relationshipName, @NonNull OddCallback<ObjectWithRelated<T>> cb) {

        try {
            T stored = (T)cache.getObject(id);
            if (stored != null) {
                List<Identifier> ids = stored.getIdentifiersByRelationship(relationshipName);
                if(ids != null) {
                    List<OddObject> relatedList = new ArrayList<>(ids.size());
                    for (Identifier identifier : ids) {
                        OddObject related = cache.getObject(identifier);
                        if(related != null)
                            relatedList.add(related);
                        else
                            return false; // at least one object was null so the cb could not be completed from the cache
                    }
                    cb.onSuccess(new ObjectWithRelated<T>(stored, relatedList));
                    return true;
                }
            }
        } catch (Exception e) {
            cb.onFailure(e);
        }
        return false;
    }

    /** returns a list of all objects which are both included in this object and included in a 1st level relationship */
    private List<OddObject> getAllRelated(OddObject object) {
        Set<OddObject> related = new LinkedHashSet<>();
        for (Relationship relationship : object.getRelationships()) {
            List<OddObject> relationshipEntities = object.getIncludedByRelationship(relationship.getName());
            related.addAll(relationshipEntities);
        }
        return new ArrayList<>(related);
    }

    protected class RequestCallback<T> implements Callback {

        private static final int MS_IN_SECONDS = 1000;
        private final ParseCall<T> parseCall;
        private final OddCallback<T> cb;

        public RequestCallback(final OddCallback<T> cb, final ParseCall<T> parseCall) {
            this.cb = cb;
            this.parseCall = parseCall;
        }

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
                handleOk(response);
            } else {
                handleNotOk(response);
            }
        }

        private void handleNotOk(Response response) {
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

        private void handleOk(Response response) {
            T obj = null;
            Exception exceptionCaught = null;
            String bodyString = null;
            try {
                obj = parseCall.parse(response.body().string());
                attemptCache(response, obj);
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
        }

        /** only caches if obj is OddObject and the response header allows for caching. */
        private void attemptCache(Response response, T obj) {
            if(obj instanceof OddObject &&
                    (!response.cacheControl().noCache() || !response.cacheControl().noStore())) {
                cache.storeObject((OddObject) obj, response.cacheControl().maxAgeSeconds() * MS_IN_SECONDS);
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
    }

    protected interface ParseCall<T> {
        T parse(String responseBody) throws JSONException;
    }

    public class ObjectWithRelated<T extends OddObject> {
        private final List<OddObject> related;
        private final T oddObject;

        public ObjectWithRelated(@NonNull T oddObject, @NonNull List<OddObject> related) {
            this.oddObject = oddObject;
            this.related = related;
        }

        @NonNull public List<OddObject> getRelated() {
            return related;
        }

        @NonNull public T getOddObject() {
            return oddObject;
        }
    }

    private class GatherAllRelatedCb<T extends OddObject> implements OddCallback<T> {
        private final OddCallback<ObjectWithRelated<T>> cb;

        private GatherAllRelatedCb(OddCallback<ObjectWithRelated<T>> cb) {
            this.cb = cb;
        }

        @Override
        public void onSuccess(T entity) {
            List<OddObject> related = getAllRelated(entity);
            cb.onSuccess(new ObjectWithRelated<T>(entity, related));
        }

        @Override
        public void onFailure(Exception exception) {
            cb.onFailure(exception);
        }
    }

    private class GatherRelationshipCb<T extends OddObject> implements OddCallback<T> {
        private final OddCallback<ObjectWithRelated<T>> cb;
        private final String relationshipName;

        private GatherRelationshipCb(OddCallback<ObjectWithRelated<T>> cb, String relationshipName) {
            this.cb = cb;
            this.relationshipName = relationshipName;
        }

        @Override
        public void onSuccess(T entity) {
            List<OddObject> related = entity.getIncludedByRelationship(relationshipName);
            cb.onSuccess(new ObjectWithRelated<T>(entity, related));
        }

        @Override
        public void onFailure(Exception exception) {
            cb.onFailure(exception);
        }
    }

    public void clearCache() {
        cache.clear();
    }

    /**
     * Returns true if an OddObject has included objects for each included param
     * @param oddObject
     * @param includeParams
     * @param <T>
     * @return
     */
    private <T extends OddObject> boolean objectHasAllIncluded(T oddObject, List<String> includeParams) {

        if (includeParams == null || includeParams.isEmpty()) {
            return true;
        }

        for (String i: includeParams) {
            List included = oddObject.getIncludedByRelationship(i);

            if (included.size() == 0) {
                return false;
            }
        }

        return true;
    }
}
