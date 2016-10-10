package io.oddworks.device.request;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.oddworks.device.exception.OddParseException;
import io.oddworks.device.exception.UnhandledPlayerTypeException;
import io.oddworks.device.model.Article;
import io.oddworks.device.model.AuthToken;
import io.oddworks.device.model.Config;
import io.oddworks.device.model.DeviceCodeResponse;
import io.oddworks.device.model.Event;
import io.oddworks.device.model.External;
import io.oddworks.device.model.Identifier;
import io.oddworks.device.model.Media;
import io.oddworks.device.model.MediaAd;
import io.oddworks.device.model.MediaImage;
import io.oddworks.device.model.Metric;
import io.oddworks.device.model.MetricsConfig;
import io.oddworks.device.model.OddCollection;
import io.oddworks.device.model.OddObject;
import io.oddworks.device.model.OddView;
import io.oddworks.device.model.Promotion;
import io.oddworks.device.model.Relationship;
import io.oddworks.device.model.Sharing;
import io.oddworks.device.model.players.ExternalPlayer;
import io.oddworks.device.model.players.OoyalaPlayer;
import io.oddworks.device.model.players.Player;
//todo stop swallowing exceptions

public class OddParser {
    private static final String TAG = OddParser.class.getSimpleName();
    private static final OddParser INSTANCE = new OddParser();
    private static final JSONParser JSON = JSONParser.getInstance();

    private static final String DATA = "data";
    private static final String ATTRIBUTES = "attributes";
    private static final String META = "meta";

    private OddParser(){
        // singleton
    }

    public static OddParser getInstance() {
        return INSTANCE;
    }

    public MediaImage parseMediaImage(final JSONObject data) throws JSONException {
        if (data == null) {
            return null;
        }
        String aspect16x9 = JSON.getString(data, "aspect16x9");
        String aspect4x3 = JSON.getString(data, "aspect4x3");
        String aspect3x4 = JSON.getString(data, "aspect3x4");
        String aspect1x1 = JSON.getString(data, "aspect1x1");
        String aspect2x3 = JSON.getString(data, "aspect2x3");
        String aspect16x9_w152 = JSON.getString(data, "aspect16x9_w152");
        String aspect16x9_w390 = JSON.getString(data, "aspect16x9_w390");
        String aspect16x9_w548 = JSON.getString(data, "aspect16x9_w548");
        String aspect16x9_w768 = JSON.getString(data, "aspect16x9_w768");
        String aspect16x9_w1152 = JSON.getString(data, "aspect16x9_w1152");
        String aspect16x9_w1536 = JSON.getString(data, "aspect16x9_w1536");
        String aspect16x9_w1920 = JSON.getString(data, "aspect16x9_w1920");
        String aspect16x9_w2304 = JSON.getString(data, "aspect16x9_w2304");
        String aspect16x9_w3072 = JSON.getString(data, "aspect16x9_w3072");

        return new MediaImage(aspect16x9, aspect3x4, aspect4x3, aspect1x1, aspect2x3,
                aspect16x9_w152,
                aspect16x9_w390,
                aspect16x9_w548,
                aspect16x9_w768,
                aspect16x9_w1152,
                aspect16x9_w1536,
                aspect16x9_w1920,
                aspect16x9_w2304,
                aspect16x9_w3072);
    }

    public MediaAd parseMediaAd(final JSONObject data) throws JSONException {
        try {
            JSONObject rawAds = data.getJSONObject("ads");

            HashMap<String, Object> properties = new HashMap<>();
            Iterator<String> adKeys = rawAds.keys();
            while(adKeys.hasNext()) {
                String adProperty = adKeys.next();
                if (adProperty.equals("enabled")) {
                    properties.put(adProperty, JSON.getBoolean(rawAds, adProperty));
                } else if (adProperty.equals("networkId")) {
                    properties.put(adProperty, JSON.getInt(rawAds, adProperty));
                } else {
                    properties.put(adProperty, JSON.getString(rawAds, adProperty));
                }
            }
            return new MediaAd(properties);
        } catch (Exception e) {
            return new MediaAd();
        }
    }

    public OddCollection parseCollection(final JSONObject data) throws JSONException {
        JSONObject rawAttributes = JSON.getJSONObject(data, ATTRIBUTES, true);
        JSONObject meta = JSON.getJSONObject(data, META, false);
        JSONObject images = JSON.getJSONObject(rawAttributes, "images", false);

        String id = JSON.getString(data, "id");
        String type = JSON.getString(data, "type");

        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("title", JSON.getString(rawAttributes, "title"));
        attributes.put("subtitle", JSON.getString(rawAttributes, "subtitle"));
        attributes.put("description", JSON.getString(rawAttributes, "description"));
        attributes.put("releaseDate", JSON.getDateTime(rawAttributes, "releaseDate"));
        attributes.put("mediaImage", parseMediaImage(images));

        OddCollection collection = new OddCollection(id, type);
        collection.setAttributes(attributes);
        collection.setMeta(meta);


        JSONObject relationships = JSON.getJSONObject(data, "relationships", true);

        addRelationshipsToOddObject(relationships, collection);

        collection.fillIncludedCollections();

        return collection;
    }

    /**
     * parses included and adds it to an OddObject
     * @param addTo addTo.addIncluded is called on the parsed included objects
     * @throws JSONException
     */
    private void addIncluded(OddObject addTo, JSONArray included) throws JSONException {
        for (int i = 0; i < included.length(); i++) {
            JSONObject includedObject = included.getJSONObject(i);
            String includedType = JSON.getString(includedObject, "type");

            switch (includedType) {
                case OddObject.TYPE_ARTICLE:
                    addTo.addIncluded(parseArticle(includedObject));
                    break;
                case OddObject.TYPE_COLLECTION:
                    addTo.addIncluded(parseCollection(includedObject));
                    break;
                case OddObject.TYPE_EVENT:
                    addTo.addIncluded(parseEvent(includedObject));
                    break;
                case OddObject.TYPE_EXTERNAL:
                    addTo.addIncluded(parseExternal(includedObject));
                    break;
                case OddObject.TYPE_PROMOTION:
                    addTo.addIncluded(parsePromotion(includedObject));
                    break;
                case OddObject.TYPE_LIVE_STREAM:
                case OddObject.TYPE_VIDEO:
                    addTo.addIncluded(parseMedia(includedObject));
                    break;
            }
        }
    }

    protected Event parseEvent(final JSONObject dataObject) throws JSONException {
        JSONObject rawAttributes = JSON.getJSONObject(dataObject, ATTRIBUTES, true);
        JSONObject meta = JSON.getJSONObject(dataObject, META, false);
        JSONObject images = JSON.getJSONObject(rawAttributes, "images", false);
        JSONObject ical = JSON.getJSONObject(rawAttributes, "ical", true);

        Event event = new Event(
                JSON.getString(dataObject, "id"),
                JSON.getString(dataObject, "type"));
        event.setMeta(meta);

        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("title", JSON.getString(rawAttributes, "title"));
        attributes.put("description", JSON.getString(rawAttributes, "description"));
        attributes.put("mediaImage", parseMediaImage(images));
        attributes.put("category", JSON.getString(rawAttributes, "category"));
        attributes.put("source", JSON.getString(rawAttributes, "source"));
        attributes.put("createdAt", JSON.getDateTime(rawAttributes, "createdAt"));
        attributes.put("url", JSON.getString(rawAttributes, "url"));

        attributes.put("dateTimeStart", JSON.getDateTime(ical, "dtstart"));
        attributes.put("dateTimeEnd", JSON.getDateTime(ical, "dtend"));
        attributes.put("location", JSON.getString(ical, "location"));

        event.setAttributes(attributes);

        return event;
    }

    protected External parseExternal(final JSONObject dataObject) throws JSONException {
        JSONObject rawAttributes = JSON.getJSONObject(dataObject, ATTRIBUTES, true);
        JSONObject meta = JSON.getJSONObject(dataObject, META, false);
        JSONObject images = JSON.getJSONObject(rawAttributes, "images", false);

        External external = new External(
                JSON.getString(dataObject, "id"),
                JSON.getString(dataObject, "type"));
        external.setMeta(meta);

        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("title", JSON.getString(rawAttributes, "title"));
        attributes.put("description", JSON.getString(rawAttributes, "description"));
        attributes.put("mediaImage", parseMediaImage(images));
        attributes.put("url", JSON.getString(rawAttributes, "url"));

        external.setAttributes(attributes);

        return external;
    }

    protected Article parseArticle(final JSONObject dataObject) throws JSONException {
        JSONObject rawAttributes = JSON.getJSONObject(dataObject, ATTRIBUTES, true);
        JSONObject meta = JSON.getJSONObject(dataObject, META, false);
        JSONObject images = JSON.getJSONObject(rawAttributes, "images", false);

        Article article = new Article(
                JSON.getString(dataObject, "id"),
                JSON.getString(dataObject, "type"));
        article.setMeta(meta);

        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("title", JSON.getString(rawAttributes, "title"));
        attributes.put("description", JSON.getString(rawAttributes, "description"));
        attributes.put("mediaImage", parseMediaImage(images));
        attributes.put("category", JSON.getString(rawAttributes, "category"));
        attributes.put("source", JSON.getString(rawAttributes, "source"));
        attributes.put("createdAt", JSON.getDateTime(rawAttributes, "createdAt"));
        attributes.put("url", JSON.getString(rawAttributes, "url"));

        article.setAttributes(attributes);

        return article;
    }

    protected Media parseMedia(final JSONObject dataObject) throws JSONException {
        JSONObject rawAttributes = JSON.getJSONObject(dataObject, ATTRIBUTES, true);
        JSONObject meta = JSON.getJSONObject(dataObject, META, false);
        JSONObject images = JSON.getJSONObject(rawAttributes, "images", false);

        Media media = new Media(
                JSON.getString(dataObject, "id"),
                JSON.getString(dataObject, "type"));
        media.setMeta(meta);

        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("title", JSON.getString(rawAttributes, "title"));
        attributes.put("subtitle", JSON.getString(rawAttributes, "subtitle"));
        attributes.put("description", JSON.getString(rawAttributes, "description"));
        attributes.put("releaseDate", JSON.getDateTime(rawAttributes, "releaseDate"));
        try {
            attributes.put("duration", JSON.getInt(rawAttributes, "duration"));
        } catch (Exception e) {
            Log.d(TAG, "Invalid duration: " + e.toString());
            attributes.put("duration", 0);
        }

        attributes.put("url", JSON.getString(rawAttributes, "url"));
        attributes.put("isLive", JSON.getString(rawAttributes, "isLive"));
        attributes.put("mediaImage", parseMediaImage(images));
        attributes.put("mediaAd", parseMediaAd(rawAttributes));
        media.setAttributes(attributes);
        JSONObject relationships = JSON.getJSONObject(dataObject, "relationships", false);
        if(relationships != null) addRelationshipsToOddObject(relationships, media);
        media.setPlayer(parsePlayer(JSON.getJSONObject(rawAttributes, "player", true)));
        media.setSharing(parseSharing(JSON.getJSONObject(rawAttributes, "sharing", false)));

        return media;
    }

    protected Player parsePlayer(final JSONObject rawPlayer) throws JSONException {
        Player.PlayerType type;
        try {
            type = Player.PlayerType.valueOf(JSON.getString(rawPlayer, "type").toUpperCase());
        } catch(IllegalArgumentException e) {
            Log.e(TAG, "Parsed player does not exist", e);
            throw new UnhandledPlayerTypeException("Unsupported player type: " + rawPlayer);
        } catch(Exception e) {
            Log.e(TAG, "Unable to parse player, falling back to native", e);
            type = Player.PlayerType.NATIVE;
        }

        Player player = null;
        switch (type) {
            case BRIGHTCOVE:
                player = new Player(type);
                break;
            case EXTERNAL:
                player = parseExternalPlayer(rawPlayer);
                break;
            case NATIVE:
            case OOYALA:
                player = parseOoyalaPlayer(rawPlayer);
                break;
            default:
                throw new UnhandledPlayerTypeException("Unsupported player type: " + type);
        }
        return player;
    }

    protected Sharing parseSharing(final JSONObject rawSharing) throws JSONException {
        boolean enabled = false;
        String text = "";
        if (rawSharing != null) {
            enabled = JSON.getBoolean(rawSharing, "enabled");
            text = JSON.getString(rawSharing, "text");
        }
        return new Sharing(enabled, text);
    }

    private OoyalaPlayer parseOoyalaPlayer(JSONObject rawPlayer) throws JSONException {
        return new OoyalaPlayer(Player.PlayerType.OOYALA,
                JSON.getString(rawPlayer, "pCode"),
                JSON.getString(rawPlayer, "embedCode"),
                JSON.getString(rawPlayer, "domain"));
    }

    private ExternalPlayer parseExternalPlayer(JSONObject rawPlayer) throws JSONException {
        return new ExternalPlayer(Player.PlayerType.EXTERNAL, JSON.getString(rawPlayer, "url"));
    }

    protected Promotion parsePromotion(final JSONObject rawPromotion) throws JSONException {
        JSONObject rawAttributes = JSON.getJSONObject(rawPromotion, ATTRIBUTES, true);
        JSONObject meta = JSON.getJSONObject(rawPromotion, META, false);
        JSONObject images = JSON.getJSONObject(rawAttributes, "images", false);

        Promotion promotion = new Promotion(
                JSON.getString(rawPromotion, "id"),
                JSON.getString(rawPromotion, "type"));
        promotion.setMeta(meta);

        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("title", JSON.getString(rawAttributes, "title"));
        attributes.put("description", JSON.getString(rawAttributes, "description"));
        attributes.put("url", JSON.getString(rawAttributes, "url"));
        attributes.put("mediaImage", parseMediaImage(images));

        promotion.setAttributes(attributes);

        return promotion;
    }

    protected List<OddObject> parseEntityList(final String result) {
        ArrayList<OddObject> entities = new ArrayList<>();
        try {
            JSONObject entitiesResponse = new JSONObject(result);
            JSONArray rawEntities = entitiesResponse.getJSONArray(DATA);
            for (int i = 0; i < rawEntities.length(); i++) {
                JSONObject rawEntity = rawEntities.getJSONObject(i);
                String type = JSON.getString(rawEntity, "type");
                switch (type) {
                    case OddObject.TYPE_ARTICLE:
                        entities.add(parseArticle(rawEntity));
                        break;
                    case OddObject.TYPE_COLLECTION:
                        entities.add(parseCollection(rawEntity));
                        break;
                    case OddObject.TYPE_EVENT:
                        entities.add(parseEvent(rawEntity));
                        break;
                    case OddObject.TYPE_EXTERNAL:
                        entities.add(parseExternal(rawEntity));
                        break;
                    case OddObject.TYPE_PROMOTION:
                        entities.add(parsePromotion(rawEntity));
                        break;
                    case OddObject.TYPE_LIVE_STREAM:
                    case OddObject.TYPE_VIDEO:
                        entities.add(parseMedia(rawEntity));
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return entities;
    }

    protected MetricsConfig parseMetrics(JSONObject rawFeatures) {
        try {
            JSONObject rawMetrics = JSON.getJSONObject(rawFeatures, "metrics", true);

            ArrayList<Metric> metrics = new ArrayList<>();
            for(String key : MetricsConfig.ACTION_KEYS) {
                Map<String, Object> attributes = new HashMap<>();
                JSONObject rawMetric = JSON.getJSONObject(rawMetrics, key, true);
                Iterator<String> mkeys = rawMetric.keys();
                while(mkeys.hasNext()) {
                    String mkey = mkeys.next();
                    switch(mkey) {
                        case Metric.ENABLED:
                            attributes.put(mkey, JSON.getBoolean(rawMetric, mkey));
                            break;
                        case Metric.INTERVAL:
                            attributes.put(mkey, JSON.getInt(rawMetric, mkey));
                            break;
                        default:
                            attributes.put(mkey, JSON.getString(rawMetric, mkey));
                            break;
                    }
                }

                metrics.add(new Metric(key, attributes));
            }

            MetricsConfig metricsConfig = new MetricsConfig(metrics);

            metricsConfig.setupOddMetrics(); // MAGIC!

            return metricsConfig;
        } catch (JSONException e) {
            Log.w(TAG, "failed to parse metrics feature from config");
            return null;
        }
    }

    protected Config parseConfig(final String result) {
        try {
            JSONObject resultJSONObject = new JSONObject(result);
            JSONObject dataJSONObject = JSON.getJSONObject(resultJSONObject, DATA, true);
            JSONObject rawAttributes = JSON.getJSONObject(dataJSONObject, ATTRIBUTES, true);

            LinkedHashMap<String, String> views = new LinkedHashMap<>();
            JSONObject rawViews = JSON.getJSONObject(rawAttributes, "views", true);
            Iterator<String> viewNames = rawViews.keys();
            while(viewNames.hasNext()) {
                String viewName = viewNames.next();
                views.put(viewName, JSON.getString(rawViews, viewName));
            }

            JSONObject rawFeatures = parseFeatures(rawAttributes);
            MetricsConfig metrics = parseMetrics(rawFeatures);

            //Get app_map
            JSONObject appMap = JSON.getJSONObject(rawFeatures, "app_map", true);

            //Get styles
            JSONObject styles = JSON.getJSONObject(rawFeatures, "styles", true);

            //Get channels
            JSONObject channels = JSON.getJSONObject(rawFeatures, "livechannels", true);

            //Get Chromecast
            JSONObject chromecastOptions = JSON.getJSONObject(rawFeatures, "chromecast", true);
            String receiverId = chromecastOptions.getString("appId");

            //Get TVE
            JSONObject tve = JSON.getJSONObject(rawFeatures, "tve_authentication", true);

            //Get Schedules
            JSONObject schedules = JSON.getJSONObject(rawFeatures, "schedules", true);
            String schedulesUri = schedules.getString("uri");

            //Get ErrorCollection
            JSONObject errorCollectionJson = JSON.getJSONObject(rawFeatures, "error_collection", false);
            String errorCollection = null;
            if (errorCollectionJson != null) {
                errorCollection = errorCollectionJson.toString();
            }

            boolean authEnabled = isAuthEnabled(rawFeatures);
            return new Config(views, authEnabled, metrics, appMap.toString(), styles.toString(), channels.toString(), receiverId, tve.toString(), schedulesUri, errorCollection);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private JSONObject parseFeatures(JSONObject rawAttributes) throws JSONException {
        return JSON.getJSONObject(rawAttributes, "features", true);
    }

    private boolean isAuthEnabled(JSONObject rawFeatures) throws JSONException {
        try {
            JSONObject rawAuth = JSON.getJSONObject(rawFeatures, "authentication", true);
            return JSON.getBoolean(rawAuth, "enabled");
        } catch (JSONException e) {
            return false;
        }
    }

    protected OddView parseViewResponse(final String result) {
        try {
            JSONObject resultObject = new JSONObject(result);
            JSONObject data = JSON.getJSONObject(resultObject, DATA, true);
            JSONObject meta = JSON.getJSONObject(resultObject, META, false);
            JSONObject rawAttributes = JSON.getJSONObject(data, ATTRIBUTES, true);
            OddView view = new OddView(
                    JSON.getString(data, "id"),
                    JSON.getString(data, "type"));
            view.setMeta(meta);

            HashMap<String, Object> attributes = new HashMap<>();
            attributes.put("title", JSON.getString(rawAttributes, "title"));
            view.setAttributes(attributes);

            // use relationships to build view's ArrayList<Relationship>
            addRelationshipsToOddObject(JSON.getJSONObject(data, "relationships", true), view);

            // fill the view's included{Type} arrays with parsable objects
            addIncludedFromResponse(view, resultObject);
            // backfill newly created collections with newly created entities
            view.fillIncludedCollections();

            return view;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected AuthToken parseAuthToken(String responseBody) throws JSONException {
        JSONObject raw = new JSONObject(responseBody);
        JSONObject data = JSON.getJSONObject(raw, DATA, true);
        JSONObject attributes = JSON.getJSONObject(data, ATTRIBUTES, true);
        JSONObject deviceUserProfile = JSON.getJSONObject(attributes, "deviceUserProfile", true);
        JSONObject entitlementCredentials = JSON.getJSONObject(deviceUserProfile, "entitlementCredentials", false);
        String accessToken = attributes.getString("access_token");
        String tokenType = attributes.getString("token_type");
        return new AuthToken(accessToken, tokenType, entitlementCredentials);
    }

    protected DeviceCodeResponse parseDeviceCodeResponse(String responseBody) throws JSONException {
        JSONObject raw = new JSONObject(responseBody);
        JSONObject data = JSON.getJSONObject(raw, DATA, true);
        JSONObject attributes = JSON.getJSONObject(data, ATTRIBUTES, true);
        String deviceCode = attributes.getString("device_code");
        String userCode = attributes.getString("user_code");
        String verificationUrl = attributes.getString("verification_url");
        int expiresIn = attributes.getInt("expires_in");
        int interval = attributes.getInt("interval");
        return new DeviceCodeResponse(deviceCode, userCode, verificationUrl, expiresIn, interval);
    }

    public OddCollection parseCollectionResponse(String responseBody) {
        OddCollection collection = null;
        try {
            JSONObject raw = new JSONObject(responseBody);
            JSONObject data = JSON.getJSONObject(raw, DATA, true);
            collection = parseCollection(data);
            addIncludedFromResponse(collection, raw);
        } catch (Exception e) {
            throw new OddParseException(e);
        }
        return collection;
    }

    protected List<OddObject> parseSearch(final String result) {
        ArrayList<OddObject> searchResult = new ArrayList<>();
        try {
            JSONObject resultObject = new JSONObject(result);
            JSONArray dataArray = resultObject.getJSONArray(DATA);
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject item = dataArray.getJSONObject(i);
                String type = JSON.getString(item, "type");

                switch(type) {
                    case OddObject.TYPE_VIDEO:
                    case OddObject.TYPE_LIVE_STREAM:
                        Media video = parseMedia(item);
                        if (video != null) {
                            searchResult.add(video);
                        }
                        break;
                    case OddObject.TYPE_ARTICLE:
                        Article article = parseArticle(item);
                        if (article != null) {
                            searchResult.add(article);
                        }
                        break;
                    case OddObject.TYPE_EVENT:
                        Event event = parseEvent(item);
                        if (event != null) {
                            searchResult.add(event);
                        }
                        break;
                    case OddObject.TYPE_EXTERNAL:
                        External external = parseExternal(item);
                        if (external != null) {
                            searchResult.add(external);
                        }
                        break;
                    case OddObject.TYPE_COLLECTION:
                        OddCollection collection = parseCollection(item);
                        if (collection != null) {
                                searchResult.add(collection);
                        }
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return searchResult;
    }

    protected void addRelationshipsToOddObject(JSONObject relationships, OddObject relatable) {
        Iterator<String> relationshipNames = relationships.keys();
        try {
            while (relationshipNames.hasNext()) {
                String name = relationshipNames.next();
                JSONObject relationship = JSON.getJSONObject(relationships, name, true);
                if (relationship != null) {
                    ArrayList<Identifier> identifiers = new ArrayList<>();

                    if (relationship.get(DATA) instanceof JSONObject) {
                        //handle single relationship.data
                        JSONObject identifier = JSON.getJSONObject(relationship, DATA, true);
                        String relId = identifier.getString("id");
                        String relType = identifier.getString("type");
                        identifiers.add(new Identifier(relId, relType));

                    } else if (relationship.get(DATA) instanceof JSONArray) {
                        // handle multiple relationship.data
                        JSONArray rawIdentifiers = JSON.getJSONArray(relationship, DATA, true);

                        for (int i = 0; i < rawIdentifiers.length(); i++) {
                            JSONObject identifier = rawIdentifiers.getJSONObject(i);
                            String relId = identifier.getString("id");
                            String relType = identifier.getString("type");
                            identifiers.add(new Identifier(relId, relType));
                        }
                    }

                    relatable.addRelationship(new Relationship(name, identifiers));
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    protected String parseErrorMessage(String responseBody) throws JSONException {
        return new JSONObject(responseBody).getString("message");
    }

    /** adds included objects from response to the OddObject if there are included objects in the response. Otherwise
     * does nothing.
     */
    protected void addIncludedFromResponse(OddObject oddObject, JSONObject response) throws JSONException {
        JSONArray included = JSON.getJSONArray(response, "included", false);
        if(included != null) addIncluded(oddObject, included);
    }

    protected Media parseMediaResponse(String responseBody) {
        Media media = null;
        try {
            JSONObject response = new JSONObject(responseBody);
            JSONObject data = JSON.getJSONObject(response, DATA, true);
            media = parseMedia(data);
            addIncludedFromResponse(media, response);
        } catch (Throwable e) {
            throw new OddParseException(e);
        }
        return media;
    }

    protected Promotion parsePromotionResponse(String responseBody) {
        Promotion promotion = null;
        try {
            JSONObject response = new JSONObject(responseBody);
            JSONObject data = JSON.getJSONObject(response, DATA, true);
            promotion = parsePromotion(data);
            addIncludedFromResponse(promotion, response);
        } catch (Throwable e) {
            throw new OddParseException(e);
        }
        return promotion;
    }

    protected External parseExternalResponse(String responseBody) {
        External external = null;
        try {
            JSONObject response = new JSONObject(responseBody);
            JSONObject data = JSON.getJSONObject(response, DATA, true);
            external = parseExternal(data);
            addIncludedFromResponse(external, response);
        } catch (Throwable e) {
            throw new OddParseException(e);
        }
        return external;
    }

    protected Event parseEventResponse(String responseBody) {
        Event event = null;
        try {
            JSONObject response = new JSONObject(responseBody);
            JSONObject data = JSON.getJSONObject(response, DATA, true);
            event = parseEvent(data);
            addIncludedFromResponse(event, response);
        } catch (Throwable e) {
            throw new OddParseException(e);
        }
        return event;
    }

    protected Article parseArticleResponse(String responseBody) {
        Article article = null;
        try {
            JSONObject response = new JSONObject(responseBody);
            JSONObject data = JSON.getJSONObject(response, DATA, true);
            article = parseArticle(data);
            addIncludedFromResponse(article, response);
        } catch (Throwable e) {
            throw new OddParseException(e);
        }
        return article;
    }
}
